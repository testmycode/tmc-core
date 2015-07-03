
package hy.tmc.core.configuration;

import hy.tmc.core.domain.Course;

public class ClientTmcSettings implements TmcSettings {
    private String serverAddress;
    private String username;
    private String password;
    private Boolean userDataExists;
    private Course currentCourse;
    private String apiVersion;

    @Override
    public String getServerAddress() {
        return serverAddress;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean userDataExists() {
        return userDataExists;
    }

    @Override
    public Course getCurrentCourse() {
        return currentCourse;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserDataExists(Boolean userDataExists) {
        this.userDataExists = userDataExists;
    }

    public void setCurrentCourse(Course currentCourse) {
        this.currentCourse = currentCourse;
    }

    @Override
    public String apiVersion() {
        return "api_version=" + apiVersion;
    }
}
