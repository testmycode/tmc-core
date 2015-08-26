package fi.helsinki.cs.tmc.core.configuration;

import fi.helsinki.cs.tmc.core.domain.Course;

import com.google.common.base.Optional;

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

    String getFormattedUserData();

    /**
     * Return the directory where course directories will be located. Projects
     * will be placed as follows: maindirectory/courseName/exerciseName
     */
    String getTmcMainDirectory();

    public static class SavedEvent {

        public SavedEvent() {
        }
    }
}
