package hy.tmc.core.configuration;

import hy.tmc.core.domain.Course;

import com.google.common.base.Optional;

public interface TmcSettings {

    public String getServerAddress();

    public String getPassword();

    public String getUsername();

    /**
     * Checks that username and password are not null.
     */
    public boolean userDataExists();

    public Optional<Course> getCurrentCourse();

    public String apiVersion();

    public String clientName();

    public String clientVersion();

    public String getFormattedUserData();

    /**
     * Return the directory where course directories will be located. Projects
     * will be placed as follows: maindirectory/courseName/exerciseName
     */
    public String getTmcMainDirectory();
}
