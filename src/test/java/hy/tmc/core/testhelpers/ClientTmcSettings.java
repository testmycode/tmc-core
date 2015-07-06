
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
