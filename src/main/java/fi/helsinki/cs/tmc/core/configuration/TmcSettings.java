package fi.helsinki.cs.tmc.core.configuration;

import fi.helsinki.cs.tmc.core.domain.Course;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;

import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.nio.file.Path;
import java.util.Locale;

public interface TmcSettings {

    String getServerAddress();

    String getPassword();

    String getUsername();

    /**
     * Checks that username and password are not null.
     */
    boolean userDataExists();

    Optional<Course> getCurrentCourse();

    String apiVersion();

    String clientName();

    String clientVersion();

    // TODO: what is this even?
    String getFormattedUserData();

    /**
     * Return the directory where course directories will be located. Projects
     * will be placed as follows: maindirectory/courseName/exerciseName
     */
    Path getTmcProjectDirectory();

    Locale getLocale();

    SystemDefaultRoutePlanner proxy();

    // For testing at least
    @Beta
    void setCourse(Course theCourse);

    @Beta
    void setConfigRoot(Path configRoot);

    Path getConfigRoot();

    String getOauthTokenUrl();

    String getOauthApplicationId();

    String getOauthSecret();
}
