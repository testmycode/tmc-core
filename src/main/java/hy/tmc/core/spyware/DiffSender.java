package hy.tmc.core.spyware;

import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiffSender {
    private TmcSettings settings;
    private UrlCommunicator urlCommunicator;

    public DiffSender(TmcSettings settings) {
        this.settings = settings;
        this.urlCommunicator = new UrlCommunicator(settings);
    }

    public DiffSender(UrlCommunicator urlCommunicator, TmcSettings settings) {
        this.settings = settings;
        this.urlCommunicator = urlCommunicator;
    }

    /**
     * Sends given byte-data to all URLs specified by course.
     *
     * @param diffs as byte-array
     * @param currentCourse tell all spywareUrls
     * @return all results
     */
    public List<HttpResult> sendToSpyware(byte[] diffs, Course currentCourse)
            throws TmcCoreException {
        List<String> spywareUrls = currentCourse.getSpywareUrls();
        List<HttpResult> results = new ArrayList<>();
        for (String url : spywareUrls) {
            results.add(sendToUrl(diffs, url));
        }
        return results;
    }

    /**
     * Sends diff-data to url.
     *
     * @param diffs as
     * @param url of destination
     * @return HttpResult from UrlCommunicator
     */
    public HttpResult sendToUrl(byte[] diffs, String url) throws TmcCoreException {
        try {
            HttpResult result =
                    urlCommunicator.makePostWithByteArray(
                            url, diffs, createHeaders(), new HashMap<String, String>());
            return result;
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            return new HttpResult(ex.getMessage(), 500, false);
        }
    }

    private Map<String, String> createHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Tmc-Version", "1");
        headers.put("X-Tmc-Username", settings.getUsername());
        headers.put("X-Tmc-Password", settings.getPassword());
        return headers;
    }
}
