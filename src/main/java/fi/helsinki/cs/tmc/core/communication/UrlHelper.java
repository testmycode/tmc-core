package fi.helsinki.cs.tmc.core.communication;

import static fi.helsinki.cs.tmc.core.communication.TmcConstants.API_VERSION_PARAM;
import static fi.helsinki.cs.tmc.core.communication.TmcConstants.CLIENT_NAME_PARAM;
import static fi.helsinki.cs.tmc.core.communication.TmcConstants.CLIENT_VERSION_PARAM;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import java.net.URI;

import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;

public class UrlHelper {

    public final String apiParam;
    public final String clientParam;
    public final String coursesExtension;

    private final TmcSettings settings;

    public UrlHelper(TmcSettings settings) {
        apiParam = "api_version=" + settings.apiVersion();
        String clientVersion = "&client_version=" + settings.clientVersion();
        clientParam = "client=" + settings.clientName() + clientVersion;
        coursesExtension = "/courses.json?" + apiParam + "&" + clientParam;
        this.settings = settings;
    }

    public URI getCourseUrl(int courseId) throws URISyntaxException {
        return withParams(URI.create(
				settings.getServerAddress() + "/courses/" + courseId + ".json"));
    }

    public URI getCourseUrl(Course course) throws URISyntaxException {
        return withParams(course.getDetailsUrl());
    }

    public String allCoursesAddress(String serverAddress) {
        return serverAddress + this.coursesExtension;
    }

    public URI withParams(URI url) throws URISyntaxException {
        return new URIBuilder(url)
                .setParameter(API_VERSION_PARAM, settings.apiVersion())
                .setParameter(CLIENT_NAME_PARAM, settings.clientName())
                .setParameter(CLIENT_VERSION_PARAM, settings.clientVersion())
                .build();
    }
}
