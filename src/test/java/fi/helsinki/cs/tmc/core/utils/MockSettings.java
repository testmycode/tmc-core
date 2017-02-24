package fi.helsinki.cs.tmc.core.utils;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;

import com.google.common.base.Optional;

import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.lang.UnsupportedOperationException;
import java.nio.file.Path;
import java.util.Locale;

public class MockSettings implements TmcSettings {

    private Optional<String> token;

    public MockSettings() {
        token = Optional.absent();
    }

    @Override
    public String getServerAddress() {
        return "testAddress";
    }

    @Override
    public Optional<String> getPassword() {
        return Optional.absent();
    }

    @Override
    public void setPassword(Optional<String> password) {

    }

    @Override
    public String getUsername() {
        return "testUsername";
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
        throw new UnsupportedOperationException();
    }

    @Override
    public String clientName() {
        return "testClient";
    }

    @Override
    public String clientVersion() {
        return "testClient";
    }

    @Override
    public String getFormattedUserData() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public String getOauthTokenUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getOauthApplicationId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOauthApplicationId(String oauthApplicationId) {

    }

    @Override
    public String getOauthSecret() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOauthSecret(String oauthSecret) {

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
