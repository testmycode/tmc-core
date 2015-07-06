package hy.tmc.core.communication;

import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;

public class UrlHelper {

    public final String apiParam;
    public final String coursesExtension;
    public final String authExtension;
    private final TmcSettings settings;
    
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
}
