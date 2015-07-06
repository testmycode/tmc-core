package hy.tmc.core.testhelpers;

import com.google.common.base.Optional;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.zipping.DefaultRootDetector;
import hy.tmc.core.zipping.ProjectRootFinder;
import java.io.IOException;

public class ClientTmcSettings implements TmcSettings {

    private String serverAddress;
    private String username;
    private String password;
    private Boolean userDataExists;
    private Course currentCourse;
    private String apiVersion;

    public ClientTmcSettings() {
        apiVersion = "7";
    }

    public ClientTmcSettings(String uname, String pword) {
        this();
        this.username = uname;
        this.password = pword;
    }

    public ClientTmcSettings(String uname, String pword, String url) {
        this(uname, pword);
        this.serverAddress = url;
    }

    public ClientTmcSettings(Course course) {
        this();
        this.currentCourse = course;
    }

    public ClientTmcSettings(String uname, String pword, Course course) {
        this(uname, pword);
        this.currentCourse = course;
    }

    @Override
    public synchronized String getServerAddress() {
        return serverAddress;
    }

    @Override
    public synchronized String getFormattedUserData() {
        return this.username + ":" + this.password;
    }

    @Override
    public synchronized String getPassword() {
        return password;
    }

    @Override
    public synchronized String getUsername() {
        return username;
    }

    @Override
    public synchronized boolean userDataExists() {
        return !(this.username.isEmpty() || this.password.isEmpty());
    }

    @Override
    public synchronized Course getCurrentCourse() {
        return currentCourse;
    }

    public synchronized void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public synchronized void setUsername(String username) {
        this.username = username;
    }

    public synchronized void setPassword(String password) {
        this.password = password;
    }

    public synchronized void setUserDataExists(Boolean userDataExists) {
        this.userDataExists = userDataExists;
    }

    public synchronized void setCurrentCourse(Course currentCourse) {
        this.currentCourse = currentCourse;
    }

    @Override
    public synchronized String apiVersion() {
        return "api_version=" + apiVersion;
    }
}
