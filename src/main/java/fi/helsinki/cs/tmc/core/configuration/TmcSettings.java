package fi.helsinki.cs.tmc.core.configuration;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.OauthCredentials;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;

import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.nio.file.Path;
import java.util.Locale;

public interface TmcSettings {

    String getServerAddress();

    void setServerAddress(String address);

    /**
     * Used for old login credentials, new ones use oauth.
     */
    Optional<String> getPassword();

    void setPassword(Optional<String> password);

    String getUsername();

    /**
     * Checks that username and password are not null.
     */
    boolean userDataExists();

    Optional<Course> getCurrentCourse();

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

    String hostProgramName();

    String hostProgramVersion();

    boolean getSendDiagnostics();

    OauthCredentials getOauthCredentials();

    void setOauthCredentials(OauthCredentials credentials);

    void setToken(String token);

    Optional<String> getToken();

    String getOrganization();

    void setOrganization(String organization);
}
