package fi.helsinki.cs.tmc.core.communication.http;

import fi.helsinki.cs.tmc.core.exceptions.ConnectionFailedException;
import fi.helsinki.cs.tmc.core.exceptions.FailedHttpResponseException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.NoConnectionReuseStrategy;
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
import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

/**
 * Downloads a single file over HTTP into memory while being cancellable.
 *
 * <p>If the response was not a successful one (status code 2xx) then a
 * {@link FailedHttpResponseException} with a preloaded buffered entity is
 * thrown.
 */
/*package*/ class HttpRequestExecutor implements Callable<BufferedHttpEntity> {

    private static final int DEFAULT_TIMEOUT = 10 * 1000;
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestExecutor.class);

    private final Object shutdownLock = new Object();

    private int timeout = DEFAULT_TIMEOUT;
    private HttpUriRequest request;

    /*package*/ HttpRequestExecutor(URI url) {
        this(new HttpGet(url));
    }

    /*package*/ HttpRequestExecutor(HttpUriRequest request) {
        this.request = request;
    }

    public HttpRequestExecutor setTimeout(int timeoutMs) {
        this.timeout = timeoutMs;
        return this;
    }

    @Override
    public BufferedHttpEntity call()
            throws IOException, InterruptedException, FailedHttpResponseException, ConnectionFailedException {
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

    private CloseableHttpClient makeHttpClient() {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout).build();
        HttpClientBuilder httpClientBuilder =
                HttpClients.custom()
                        .useSystemProperties()
                        .setConnectionReuseStrategy(new NoConnectionReuseStrategy())
                        .setRedirectStrategy(new DefaultRedirectStrategy())
                        .setDefaultRequestConfig(config);
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
            throws IOException, InterruptedException, FailedHttpResponseException, ConnectionFailedException {
        HttpResponse response;
        HttpContext context = new BasicHttpContext();

        try {
            response = httpClient.execute(request);
        } catch (IOException ex) {
            logger.info("Executing http request failed: {0}", ex.toString());
            if (request.isAborted()) {
                throw new InterruptedException();
            } else if (ex.getMessage().contains("connect timed out")) {
                throw new ConnectionFailedException("Communication with server failed! Please check your internet connection and try again.\n"
                        + "Try opening a browser and see if you can load any pages.");
            } else {
                throw new IOException("Download failed: " + ex.getMessage(), ex);
            }
        }

        return handleResponse(response);
    }

    private BufferedHttpEntity handleResponse(HttpResponse response)
            throws IOException, FailedHttpResponseException {
        int responseCode = response.getStatusLine().getStatusCode();
        if (response.getEntity() == null) {
            throw new IOException("HTTP " + responseCode + " with no response");
        }

        BufferedHttpEntity entity = new BufferedHttpEntity(response.getEntity());
        EntityUtils.consume(entity); // Ensure it's loaded into memory
        if (200 <= responseCode && responseCode <= 299) {
            return entity;
        } else {
            logger.info(
                    "Received http response with non 2xx response code "
                            + responseCode
                            + " with body \""
                            + entity
                            + "\"");
            throw FailedHttpResponseException.fromResponse(responseCode, entity);
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
