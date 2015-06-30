package hy.tmc.cli.backend.communication;

import static hy.tmc.cli.backend.communication.authorization.Authorization.encode;
import static org.apache.http.HttpHeaders.USER_AGENT;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;

import hy.tmc.cli.configuration.ClientData;
import hy.tmc.cli.frontend.communication.server.ProtocolException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.message.BasicNameValuePair;
import java.io.IOException;
import java.util.Map;

public class UrlCommunicator {
    
    public static final int BAD_REQUEST = 407;

    /**
     * Creates and executes post-request to specified URL.
     *
     * @param fileBody FileBody or ByteArrayBody that includes data to be
     * sended.
     * @param destinationUrl destination of the url.
     * @param headers Headers to be added to httprequest.
     * @return HttpResult that contains response from the server.
     * @throws java.io.IOException if file is invalid.
     */
    public static HttpResult makePostWithFile(ContentBody fileBody,
            String destinationUrl, Map<String, String> headers) throws IOException {
        HttpPost httppost = new HttpPost(destinationUrl);
        addHeadersTo(httppost, headers);
        addFileToRequest(fileBody, httppost);
        return getResponseResult(httppost);
    }
    
    private static void addFileToRequest(ContentBody fileBody, HttpPost httppost) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("submission[file]", fileBody);
        addCredentials(httppost, ClientData.getFormattedUserData());
        HttpEntity entity = builder.build();
        httppost.setEntity(entity);
    }

    /**
     * Tries to make GET-request to specific url.
     *
     * @param url URL to make request to
     * @param params Any amount of parameters for the request. params[0] is
     * always username:password
     * @return A Result-object with some data and a state of success or fail
     */
    public static HttpResult makeGetRequest(String url, String... params) throws IOException {
        HttpGet httpGet = createGet(url, params);
        return getResponseResult(httpGet);
    }
    
    /**
     * Makes PUT-request to wanted Url. Key-Value parameters gets added to body. 
     * 
     * @param url where the request is sent. 
     * @param body contains key-value -pairs.
     * @return Result which contains the result.
     */
    public static HttpResult makePutRequest(String url, Optional<Map<String, String>> body) throws IOException {
            HttpPut httpPut = new HttpPut(url);
            addCredentials(httpPut, ClientData.getFormattedUserData());
            List<NameValuePair> params = new ArrayList<>();
            
            for (String key : body.get().keySet()) {
                String value = body.get().get(key);
                params.add(new BasicNameValuePair(key, value));
            }            
            httpPut.setEntity(new UrlEncodedFormEntity(params));           
            return getResponseResult(httpPut);
    }
    
    private static HttpGet createGet(String url, String[] params)
            throws IOException {
        HttpGet request = new HttpGet(url);
        addCredentials(request, params[0]);
        return request;
    }

    /**
     * Download a file from the internet.
     *
     * @param url url of the get request
     * @param file file to write the results into
     * @param params params of the get request
     * @return true if successful
     */
    public static boolean downloadFile(
            String url,
            File file,
            String... params) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            HttpGet httpget = createGet(url, params);
            HttpResponse response = executeRequest(httpget);
            fileOutputStream.write(EntityUtils.toByteArray(response.getEntity()));
            return true;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    
    private static StringBuilder writeResponse(HttpResponse response)
            throws UnsupportedOperationException, IOException {
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result;
    }
    
    private static HttpClient createClient() {
        return HttpClientBuilder.create().build();
    }
    
    private static HttpResponse executeRequest(HttpRequestBase request)
            throws IOException {
        return createClient().execute(request);
    }
    
    private static void addCredentials(HttpRequestBase httpRequest, String credentials) {
        httpRequest.setHeader("Authorization", "Basic " + encode(credentials));
        httpRequest.setHeader("User-Agent", USER_AGENT);
    }

    /**
     * Adds headers to request if present.
     *
     * @param httpRequest where to put headers.
     * @param headers to be included.
     */
    private static void addHeadersTo(HttpRequestBase httpRequest, Map<String, String> headers) {
        if (!headers.isEmpty()) {
            for (String header : headers.keySet()) {
                httpRequest.addHeader(header, headers.get(header));
            }
        }
    }
    
    private static HttpResult getResponseResult(HttpRequestBase httpRequest) throws IOException {
        HttpResponse response = executeRequest(httpRequest);
        StringBuilder result = writeResponse(response);
        int status = response.getStatusLine().getStatusCode();
        HttpResult httpResult = new HttpResult(result.toString(), status, true);
        validateHttpResult(httpResult);
        return httpResult;
    }

    /**
     * Makes a POST HTTP request.
     */
    public static HttpResult makePostWithJson(JsonObject req, String feedbackUrl)
            throws IOException {
        HttpPost httppost = new HttpPost(feedbackUrl);
        String jsonString = req.toString();
        StringEntity feedbackJson = new StringEntity(jsonString);
        httppost.addHeader("content-type", "application/json");
        addCredentials(httppost, ClientData.getFormattedUserData());
        httppost.setEntity(feedbackJson);
        return getResponseResult(httppost);
    }
    
    /**
     * Validate httpResults. More logic could/should be implemented.
     * @param result
     * @throws ProtocolException 
     */
    private static void validateHttpResult(HttpResult result) throws TmcServerException {
        int statuscode = result.getStatusCode();
        if (statuscode >= 500 && statuscode < 600) {
            throw new TmcServerException("Error occured on TMC-server: statuscode " + statuscode);
        }
    }
}
