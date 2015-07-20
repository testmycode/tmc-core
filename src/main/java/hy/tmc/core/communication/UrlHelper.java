package hy.tmc.core.communication;

import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;

public class UrlHelper {

    public final String apiParam;
    public final String coursesExtension;
    public final String authExtension;
    private final TmcSettings settings;
    private String serverAddressPattern = "(https://)?([a-z]+\\.){2,}[a-z]+(/[a-z]+)*";
    
    public UrlHelper(TmcSettings settings) {
        apiParam = "api_version=" + settings.apiVersion();
        coursesExtension = "/courses.json?" + apiParam;
        authExtension = "/user";
        this.settings = settings;
    }
    
    public String getCourseUrl(int courseId) {
        return settings.getServerAddress() + "/courses/" + courseId + ".json?" + apiParam;
    }
    
    public String getCourseUrl(Course course) {
        return course.getDetailsUrl() + apiParam;
    }

    public String allCoursesAddress(String serverAddress) {
        return serverAddress + this.coursesExtension;
    }
    
    public String withApiVersion(String url) {
        String postfix = "?api_version=" + settings.apiVersion();
        if (!url.endsWith(postfix)) {
            url += postfix;
        }
        return url;
    }
    
    public boolean urlIsValid(String url) {
        return url.matches(serverAddressPattern);
    }
}
