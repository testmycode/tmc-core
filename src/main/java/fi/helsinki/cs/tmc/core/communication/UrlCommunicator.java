package fi.helsinki.cs.tmc.core.communication;

import fi.helsinki.cs.tmc.core.communication.authorization.Authorization;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UrlCommunicator {

    private static final Logger log = LoggerFactory.getLogger(UrlCommunicator.class);

    private final String submissionKey = "submission[file]";

    private TmcSettings settings;
    private UrlHelper urlHelper;

    public UrlCommunicator(TmcSettings settings) {
        this.settings = settings;
        this.urlHelper = new UrlHelper(settings);
    }

    /**
     * Creates and executes post-request to specified URL.
     *
     * @param fileBody FileBody that includes data to be sended.
     * @param destinationUrl destination of the url.
     * @param headers Headers to be added to httprequest.
     * @return HttpResult that contains response from the server.
     * @throws java.io.IOException if file is invalid.
     */
    public HttpResult makePostWithFile(
            ContentBody fileBody, URI destinationUrl, Map<String, String> headers)
            throws IOException {
        HttpPost httppost = new HttpPost(destinationUrl);
        addHeadersTo(httppost, headers);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        addCredentials(httppost, this.settings.getFormattedUserData());
        builder = addFileToRequest(fileBody, builder);

        HttpEntity entity = builder.build();
        httppost.setEntity(entity);
        return getResponseResult(httppost);
    }

    /**
     * Adds byte-array to entity of post-request and executes it.
     */
    public HttpResult makePostWithByteArray(
            URI url, byte[] data, Map<String, String> extraHeaders, Map<String, String> params)
            throws IOException {
        HttpPost rawPost = makeRawPostRequest(url, data, extraHeaders, params);
        return getResponseResult(rawPost);
    }

    private HttpPost makeRawPostRequest(
            URI url, byte[] data, Map<String, String> extraHeaders, Map<String, String> params) {
        HttpPost request = new HttpPost(url);
        addHeadersTo(request, extraHeaders);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder = addParamsToRequest(params, builder);

        ByteArrayBody byteBody = new ByteArrayBody(data, "submission.zip");
        builder = addFileToRequest(byteBody, builder);
        HttpEntity entity = builder.build();
        addCredentials(request, this.settings.getFormattedUserData());
        request.setEntity(entity);
        return request;
    }

    /**
     * Creates and executes post-request to specified URL.
     *
     * @param fileBody FileBody or ByteArrayBody that includes data to be sended.
     * @param destinationUrl destination of the url.
     * @param headers Headers to be added to httprequest.
     * @return HttpResult that contains response from the server.
     * @throws java.io.IOException if file is invalid.
     */
    public HttpResult makePostWithFileAndParams(
            FileBody fileBody,
            URI destinationUrl,
            Map<String, String> headers,
            Map<String, String> params)
            throws IOException {
        HttpPost httppost = new HttpPost(destinationUrl);
        addHeadersTo(httppost, headers);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder = addParamsToRequest(params, builder);
        builder = addFileToRequest(fileBody, builder);
        addCredentials(httppost, this.settings.getFormattedUserData());

        HttpEntity entity = builder.build();
        httppost.setEntity(entity);
        return getResponseResult(httppost);
    }

    private MultipartEntityBuilder addFileToRequest(
            ContentBody fileBody, MultipartEntityBuilder builder) {
        builder.addPart(submissionKey, fileBody);
        return builder;
    }

    private MultipartEntityBuilder addParamsToRequest(
            Map<String, String> params, MultipartEntityBuilder builder) {
        for (Map.Entry<String, String> e : params.entrySet()) {
            builder.addTextBody(
                    e.getKey(), e.getValue(), ContentType.create("text/plain", "utf-8"));
        }
        return builder;
    }

    /**
     * Tries to make a GET-request to {@code url} with {@code credentials} and returns an object
     * representing the result.
     *
     * @param url URL to make request to
     * @param credentials to add to the request
     * @return A Result-object with some data and a state of success or fail
     */
    public HttpResult makeGetRequest(URI url, String credentials) throws IOException {
        HttpGet httpGet = createGet(url, credentials);
        return getResponseResult(httpGet);
    }

    /**
     * Makes PUT-request to wanted Url. Key-Value parameters gets added to body.
     *
     * @param url where the request is sent.
     * @param body contains key-value -pairs.
     * @return Result which contains the result.
     */
    public HttpResult makePutRequest(URI url, Optional<Map<String, String>> body)
            throws IOException, URISyntaxException {
        url = urlHelper.withParams(url);
        HttpPut httpPut = new HttpPut(url);
        addCredentials(httpPut, this.settings.getFormattedUserData());
        List<NameValuePair> params = new ArrayList<>();

        for (String key : body.get().keySet()) {
            String value = body.get().get(key);
            params.add(new BasicNameValuePair(key, value));
        }
        httpPut.setEntity(new UrlEncodedFormEntity(params));
        return getResponseResult(httpPut);
    }

    private HttpGet createGet(URI url, String credentials) {
        HttpGet request = new HttpGet(url);
        addCredentials(request, credentials);
        return request;
    }

    /**
     * Download a file from the internet.
     *
     * @param url url of the get request
     * @param path path where to download
     * @param credentials users account credentials
     * @return true if successful
     */
    public boolean downloadToFile(URI url, Path path, String credentials) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(path.toFile())) {
            HttpGet httpget = createGet(url, credentials);
            HttpResponse response = executeRequest(httpget);
            fileOutputStream.write(EntityUtils.toByteArray(response.getEntity()));
            return true;
        } catch (IOException e) {
            log.error("Download error: {}", e);
            return false;
        }
    }

    /**
     * Calls downloadToFile with username and password as params.
     */
    public boolean downloadToFile(URI url, Path path) {
        return downloadToFile(url, path, this.settings.getFormattedUserData());
    }

    private StringBuilder writeResponse(HttpResponse response)
            throws UnsupportedOperationException, IOException {
        BufferedReader rd =
                new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result;
    }

    private HttpClient createClient() {
        return HttpClientBuilder.create().build();
    }

    private HttpResponse executeRequest(HttpRequestBase request) throws IOException {
        return createClient().execute(request);
    }

    private void addCredentials(HttpRequestBase httpRequest, String credentials) {
        httpRequest.setHeader(
                HttpHeaders.AUTHORIZATION, "Basic " + Authorization.encode(credentials));
    }

    /**
     * Adds headers to request if present.
     *
     * @param httpRequest where to put headers.
     * @param headers to be included.
     */
    private void addHeadersTo(HttpRequestBase httpRequest, Map<String, String> headers) {
        if (!headers.isEmpty()) {
            for (String header : headers.keySet()) {
                httpRequest.addHeader(header, headers.get(header));
            }
        }
    }

    private HttpResult getResponseResult(HttpRequestBase httpRequest) throws IOException {
        HttpResponse response = executeRequest(httpRequest);
        StringBuilder result = writeResponse(response);
        int status = response.getStatusLine().getStatusCode();
        HttpResult httpResult = new HttpResult(result.toString(), status, true);
        new HttpStatusValidator().validate(httpResult.getStatusCode());
        return httpResult;
    }

    /**
     * Makes a POST HTTP request.
     */
    public HttpResult makePostWithJson(JsonObject req, URI feedbackUrl)
            throws IOException, URISyntaxException {
        feedbackUrl = urlHelper.withParams(feedbackUrl);
        HttpPost httppost = new HttpPost(feedbackUrl);
        String jsonString = req.toString();
        StringEntity feedbackJson = new StringEntity(jsonString);
        httppost.addHeader("content-type", "application/json");
        addCredentials(httppost, this.settings.getFormattedUserData());
        httppost.setEntity(feedbackJson);
        return getResponseResult(httppost);
    }

    public HttpResult makeGetRequestWithAuthentication(URI url) throws IOException {
        return this.makeGetRequest(url, this.settings.getFormattedUserData());
    }
}
