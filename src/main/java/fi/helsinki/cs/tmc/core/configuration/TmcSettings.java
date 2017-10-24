package fi.helsinki.cs.tmc.core.configuration;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.OauthCredentials;
import fi.helsinki.cs.tmc.core.domain.Organization;

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

    Optional<String> getUsername();

    /**
     * Checks that username and password are not null.
     */
    boolean userDataExists();

    Optional<Course> getCurrentCourse();

    String clientName();

    String clientVersion();

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

    Path getConfigRoot();

    String hostProgramName();

    String hostProgramVersion();

    boolean getSendDiagnostics();

    Optional<OauthCredentials> getOauthCredentials();

    void setOauthCredentials(Optional<OauthCredentials> credentials);

    void setToken(Optional<String> token);

    Optional<String> getToken();

    Optional<Organization> getOrganization();

    void setOrganization(Optional<Organization> organization);
}
