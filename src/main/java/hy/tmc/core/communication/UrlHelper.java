package hy.tmc.core.communication;

import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;

public class UrlHelper {

    public final String apiParam;
    public final String clientParam;
    public final String coursesExtension;
    public final String authExtension;
    private final TmcSettings settings;
    private String serverAddressPattern = "(https://)?([a-z]+\\.){2,}[a-z]+(/[a-z]+)*";

    public UrlHelper(TmcSettings settings) {
        apiParam = "api_version=" + settings.apiVersion();
        String clientVersion = "&client_version=" + settings.clientVersion();
        clientParam = "client=" + settings.clientName() + clientVersion;
        coursesExtension = "/courses.json?" + apiParam + "&" + clientParam;
        authExtension = "/user";
        this.settings = settings;
    }

    public String getCourseUrl(int courseId) {
        String params = "?" + apiParam + "&" + clientParam;
        return settings.getServerAddress() + "/courses/" + courseId + ".json" + params;
    }

    public String getCourseUrl(Course course) {
        return course.getDetailsUrl() + "?" + apiParam + "&" + clientParam;
    }

    public String allCoursesAddress(String serverAddress) {
        return serverAddress + this.coursesExtension;
    }

    public String withParams(String url) {
        String params = "?" + apiParam + "&" + clientParam;
        if (url.endsWith("?" + apiParam)) {
            url = url.substring(0, url.indexOf("?" + apiParam)) + params;
        } else if (!url.endsWith(params)) {
            url += params;
        }
        return url;
    }

    public boolean urlIsValid(String url) {
        return url.matches(serverAddressPattern);
    }
}
