package fi.helsinki.cs.tmc.core.utils;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;

import com.google.common.base.Optional;

import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.nio.file.Path;
import java.util.Locale;

public class MockSettings implements TmcSettings {

    private Optional<String> token;

    public MockSettings() {
        token = Optional.absent();
    }

    @Override
    public String getServerAddress() {
        return null;
    }

    @Override
    public Optional<String> getPassword() {
        return null;
    }

    @Override
    public void setPassword(Optional<String> password) {

    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean userDataExists() {
        return false;
    }

    @Override
    public Optional<Course> getCurrentCourse() {
        return null;
    }

    @Override
    public String apiVersion() {
        return null;
    }

    @Override
    public String clientName() {
        return null;
    }

    @Override
    public String clientVersion() {
        return null;
    }

    @Override
    public String getFormattedUserData() {
        return null;
    }

    @Override
    public Path getTmcProjectDirectory() {
        return null;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public SystemDefaultRoutePlanner proxy() {
        return null;
    }

    @Override
    public void setCourse(Course theCourse) {

    }

    @Override
    public void setConfigRoot(Path configRoot) {

    }

    @Override
    public Path getConfigRoot() {
        return null;
    }

    @Override
    public String getOauthTokenUrl() {
        return null;
    }

    @Override
    public String getOauthApplicationId() {
        return null;
    }

    @Override
    public String getOauthSecret() {
        return null;
    }

    @Override
    public void setToken(String token) {
        this.token = Optional.of(token);
    }

    @Override
    public Optional<String> getToken() {
        return token;
    }
}
