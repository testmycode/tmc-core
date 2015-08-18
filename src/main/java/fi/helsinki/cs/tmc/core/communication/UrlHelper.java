package fi.helsinki.cs.tmc.core.communication;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;

import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;

public class UrlHelper {

    public final String apiParam;
    public final String clientParam;
    public final String coursesExtension;
    private final TmcSettings settings;
    private String serverAddressPattern = "(https://)?([a-z]+\\.){2,}[a-z]+(/[a-z]+)*";

    public UrlHelper(TmcSettings settings) {
        apiParam = "api_version=" + settings.apiVersion();
        String clientVersion = "&client_version=" + settings.clientVersion();
        clientParam = "client=" + settings.clientName() + clientVersion;
        coursesExtension = "/courses.json?" + apiParam + "&" + clientParam;
        this.settings = settings;
    }

    public String getCourseUrl(int courseId) throws URISyntaxException {
        return withParams(settings.getServerAddress() + "/courses/" + courseId + ".json");
    }

    public String getCourseUrl(Course course) throws URISyntaxException {
        return withParams(course.getDetailsUrl());
    }

    public String allCoursesAddress(String serverAddress) {
        return serverAddress + this.coursesExtension;
    }

    public String withParams(String url) throws URISyntaxException {
        return new URIBuilder(url)
                .addParameter(TmcConstants.API_VERSION_PARAM, settings.apiVersion())
                .addParameter(TmcConstants.CLIENT_NAME_PARAM, settings.clientName())
                .addParameter(TmcConstants.CLIENT_VERSION_PARAM, settings.clientVersion())
                .build().toString();
    }

    public boolean urlIsValid(String url) {
        return url.matches(serverAddressPattern);
    }
}
