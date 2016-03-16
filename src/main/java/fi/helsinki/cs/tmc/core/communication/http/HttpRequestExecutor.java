package fi.helsinki.cs.tmc.core.communication.http;

import fi.helsinki.cs.tmc.core.exceptions.FailedHttpResponseException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

/**
 * Downloads a single file over HTTP into memory while being cancellable.
 *
 * If the response was not a successful one (status code 2xx) then a
 * {@link FailedHttpResponseException} with a preloaded buffered entity is
 * thrown.
 */
/*package*/ class HttpRequestExecutor implements Callable<BufferedHttpEntity> {

    private static final int DEFAULT_TIMEOUT = 30 * 1000;
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestExecutor.class);

    private final Object shutdownLock = new Object();

    private int timeout = DEFAULT_TIMEOUT;
    private HttpUriRequest request;
    private UsernamePasswordCredentials credentials; // May be null

    /*package*/ HttpRequestExecutor(String url) {
        this(new HttpGet(url));
    }

    /*package*/ HttpRequestExecutor(HttpUriRequest request) {
        this.request = request;
        if (request.getURI().getUserInfo() != null) {
            credentials = new UsernamePasswordCredentials(request.getURI().getUserInfo());
        }

    }

    public HttpRequestExecutor setCredentials(String username, String password) {
        return setCredentials(new UsernamePasswordCredentials(username, password));
    }

    public HttpRequestExecutor setCredentials(UsernamePasswordCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    public HttpRequestExecutor setTimeout(int timeoutMs) {
        return this;
    }

    @Override
    public BufferedHttpEntity call()
            throws IOException, InterruptedException, FailedHttpResponseException {
        CloseableHttpClient httpClient = makeHttpClient();

        try {
            return executeRequest(httpClient);
        } finally {
            synchronized (shutdownLock) {
                request = null;
                disposeOfHttpClient(httpClient);
            }
        }
    }

    private CloseableHttpClient makeHttpClient() throws IOException {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (credentials != null) {
            credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        }

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .useSystemProperties()
                .setConnectionReuseStrategy(new NoConnectionReuseStrategy())
                .setDefaultCredentialsProvider(credentialsProvider)
                .setRedirectStrategy(new DefaultRedirectStrategy());
        maybeSetProxy(httpClientBuilder);

        return httpClientBuilder.build();
    }

    private void disposeOfHttpClient(CloseableHttpClient httpClient) {
        try {
            httpClient.close();
        } catch (IOException ex) {
            logger.warn("Dispose of httpClient failed {0}", ex);
        }
    }

    private BufferedHttpEntity executeRequest(HttpClient httpClient)
            throws IOException, InterruptedException, FailedHttpResponseException {
        HttpResponse response;
        HttpContext context = new BasicHttpContext();

        try {
            if (this.credentials != null) {
                request.addHeader(new BasicScheme(Charset.forName("UTF-8"))
                        .authenticate(this.credentials, request, context));
            }
            response = httpClient.execute(request);
        } catch (IOException ex) {
            logger.info("Executing http request failed: {0}", ex.toString());
            if (request.isAborted()) {
                throw new InterruptedException();
            } else {
                throw new IOException("Download failed: " + ex.getMessage(), ex);
            }
        } catch (AuthenticationException ex) {
            logger.info("Auth failed {0}", ex);
            throw new InterruptedException();
        }

        return handleResponse(response);
    }

    private BufferedHttpEntity handleResponse(HttpResponse response)
            throws IOException, InterruptedException, FailedHttpResponseException {
        int responseCode = response.getStatusLine().getStatusCode();
        if (response.getEntity() == null) {
            throw new IOException("HTTP " + responseCode + " with no response");
        }

        BufferedHttpEntity entity = new BufferedHttpEntity(response.getEntity());
        EntityUtils.consume(entity); // Ensure it's loaded into memory
        if (200 <= responseCode && responseCode <= 299) {
            return entity;
        } else {
            logger.info("Received http response with non 2xx response code " + responseCode
            + " with body \"" + entity + "\"");
            throw new FailedHttpResponseException(responseCode, entity);
        }
    }

    /**
     * May be called from another thread to cancel an ongoing download.
     */
    //@Override
    //TODO: Cancellable?
    public boolean cancel() {
        synchronized (shutdownLock) {
            if (request != null) {
                request.abort();
            }
        }
        return true;
    }

    private void maybeSetProxy(HttpClientBuilder httpClientBuilder) {
        SystemDefaultRoutePlanner systemDefaultRoutePlanner = TmcSettingsHolder.get().proxy();
        if (systemDefaultRoutePlanner != null) {
            httpClientBuilder.setRoutePlanner(systemDefaultRoutePlanner);
        }
    }
}
