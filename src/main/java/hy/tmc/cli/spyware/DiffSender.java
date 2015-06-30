package hy.tmc.cli.spyware;

import hy.tmc.cli.backend.communication.HttpResult;
import hy.tmc.cli.backend.communication.UrlCommunicator;
import hy.tmc.cli.configuration.ClientData;
import hy.tmc.cli.domain.Course;
import hy.tmc.cli.frontend.communication.server.ProtocolException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;

public class DiffSender {

    /**
     * Sends given file to all URLs specified by course.
     *
     * @param diffFile includes diffs to be sended
     * @param currentCourse tell all spywareUrls
     * @return all results
     */
    public List<HttpResult> sendToSpyware(File diffFile, Course currentCourse) throws ProtocolException {
        List<String> spywareUrls = currentCourse.getSpywareUrls();
        List<HttpResult> results = new ArrayList<>();
        for (String url : spywareUrls) {
            results.add(sendToUrl(diffFile, url));
        }
        return results;
    }
    
    /**
     * Sends given byte-data to all URLs specified by course.
     *
     * @param diffs as byte-array
     * @param currentCourse tell all spywareUrls
     * @return all results
     */
    public List<HttpResult> sendToSpyware(byte[] diffs, Course currentCourse) throws ProtocolException {
        List<String> spywareUrls = currentCourse.getSpywareUrls();
        List<HttpResult> results = new ArrayList<>();
        for (String url : spywareUrls) {
            results.add(sendToUrl(diffs, url));
        }
        return results;
    }

    /**
     * Sends file to url.
     *
     * @param diffFile includes diffs to be sended
     * @param url of destination
     * @return HttpResult from UrlCommunicator
     */
    public HttpResult sendToUrl(File diffFile, String url) throws ProtocolException {
        Map<String, String> headers = createHeaders();
        HttpResult result = makePostRequest(
                new FileBody(diffFile), url, headers
        );
        return result;
    }
    
    /**
     * Sends diff-data to url.
     *
     * @param diffs as 
     * @param url of destination
     * @return HttpResult from UrlCommunicator
     */
    public HttpResult sendToUrl(byte[] diffs, String url) throws ProtocolException {
        Map<String, String> headers = createHeaders();
        HttpResult result = makePostRequest(
                new ByteArrayBody(diffs, ""), url, headers
        );
        return result;
    }

    private HttpResult makePostRequest(ContentBody diffFile, String url, Map<String, String> headers) throws ProtocolException {
        HttpResult result = null;
        try {
            result = UrlCommunicator.makePostWithFile(diffFile, url, headers);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        return result;
    }
    
    private Map<String, String> createHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Tmc-Version", "1");
        headers.put("X-Tmc-Username", ClientData.getUsername());
        headers.put("X-Tmc-Password", ClientData.getPassword());
        return headers;
    }
}