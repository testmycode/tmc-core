package fi.helsinki.cs.tmc.core.communication.http;

import fi.helsinki.cs.tmc.core.exceptions.FailedHttpResponseException;

import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Convenient methods to start asynchronous HTTP tasks.
 *
 * <p>Tasks throw a {@link FailedHttpResponseException} when getting a response
 * with a non-successful status code.
 */
public class HttpTasks {
    private static final ContentType UTF8_TEXT_CONTENT_TYPE =
            ContentType.create("text/plain", "utf-8");

    private UsernamePasswordCredentials credentials = null;

    public HttpTasks setCredentials(String username, String password) {
        this.credentials = new UsernamePasswordCredentials(username, password);
        return this;
    }

    private HttpRequestExecutor createExecutor(URI url) {
        return new HttpRequestExecutor(url).setCredentials(credentials);
    }

    private HttpRequestExecutor createExecutor(HttpPost request) {
        return new HttpRequestExecutor(request).setCredentials(credentials);
    }

    public Callable<byte[]> getForBinary(URI url) {
        return downloadToBinary(createExecutor(url));
    }

    public Callable<String> getForText(URI url) {
        return downloadToText(createExecutor(url));
    }

    public Callable<byte[]> postForBinary(URI url, Map<String, String> params) {
        return downloadToBinary(createExecutor(makePostRequest(url, params)));
    }

    public Callable<String> postForText(URI url, Map<String, String> params) {
        return downloadToText(createExecutor(makePostRequest(url, params)));
    }

    public Callable<String> rawPostForText(URI url, byte[] data) {
        return downloadToText(createExecutor(makeRawPostRequest(url, data)));
    }

    public Callable<String> rawPostForText(URI url, byte[] data, Map<String, String> extraHeaders) {
        return downloadToText(createExecutor(makeRawPostRequest(url, data, extraHeaders)));
    }

    public Callable<String> uploadFileForTextDownload(
            URI url, Map<String, String> params, String fileField, byte[] data) {
        HttpPost request = makeFileUploadRequest(url, params, fileField, data);
        return downloadToText(createExecutor(request));
    }

    private Callable<byte[]> downloadToBinary(final HttpRequestExecutor download) {
        return new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return EntityUtils.toByteArray(download.call());
            }

            //TODO: Cancellable?
        };
    }

    private Callable<String> downloadToText(final HttpRequestExecutor download) {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                return EntityUtils.toString(download.call(), "UTF-8");
            }

            //TODO: Cancellable?
        };
    }

    private HttpPost makePostRequest(URI url, Map<String, String> params) {
        HttpPost request = new HttpPost(url);

        ArrayList<NameValuePair> pairs = new ArrayList<>(params.size());
        for (Map.Entry<String, String> param : params.entrySet()) {
            pairs.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }

        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs, "UTF-8");
            request.setEntity(entity);
            return request;
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private HttpPost makeRawPostRequest(URI url, byte[] data) {
        Map<String, String> empty = Collections.emptyMap();
        return makeRawPostRequest(url, data, empty);
    }

    private HttpPost makeRawPostRequest(URI url, byte[] data, Map<String, String> extraHeaders) {
        HttpPost request = new HttpPost(url);
        for (Map.Entry<String, String> header : extraHeaders.entrySet()) {
            request.addHeader(header.getKey(), header.getValue());
        }

        ByteArrayEntity entity = new ByteArrayEntity(data);
        request.setEntity(entity);
        return request;
    }

    private HttpPost makeFileUploadRequest(
            URI url, Map<String, String> params, String fileField, byte[] data) {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();

        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        for (Map.Entry<String, String> e : params.entrySet()) {
            entityBuilder.addTextBody(e.getKey(), e.getValue(), UTF8_TEXT_CONTENT_TYPE);
        }

        entityBuilder.addPart(fileField, new ByteArrayBody(data, "file"));

        HttpPost request = new HttpPost(url);

        request.setEntity(entityBuilder.build());
        return request;
    }
}
