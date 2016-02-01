package fi.helsinki.cs.tmc.core.configuration;

import fi.helsinki.cs.tmc.core.domain.Course;

import com.google.common.base.Optional;

import java.nio.file.Path;

public interface TmcSettings {

    String getServerAddress();

    // TODO:  abstract outh login + passwd as it may be login + oauth token soon
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

    // TODO: fix, it returns: `username:password`
    String getFormattedUserData();

    /**
     * Return the directory where course directories will be located. Projects
     * will be placed as follows: maindirectory/courseName/exerciseName
     */
    // TODO: rename
    Path getTmcMainDirectory();
}
