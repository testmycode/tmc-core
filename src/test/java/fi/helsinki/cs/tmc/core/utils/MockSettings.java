package fi.helsinki.cs.tmc.core.utils;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.OauthCredentials;
import fi.helsinki.cs.tmc.core.domain.Organization;

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
    public void setServerAddress(String address) {

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
    public String clientName() {
        return "testClient";
    }

    @Override
    public String clientVersion() {
        return "testVersion";
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
        return new Locale("en");
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
    public String hostProgramName() {
        return "testHostProgram";
    }

    @Override
    public String hostProgramVersion() {
        return "testHostProgramVersion";
    }

    @Override
    public boolean getSendDiagnostics() {
        return true;
    }

    @Override
    public OauthCredentials getOauthCredentials() {
        OauthCredentials credentials = new OauthCredentials();
        credentials.setOauthApplicationId("testOauthApplicationId");
        credentials.setOauthSecret("testOauthSecret");
        return credentials;
    }

    @Override
    public void setOauthCredentials(OauthCredentials credentials) {

    }

    @Override
    public void setToken(String token) {
        this.token = Optional.of(token);
    }

    @Override
    public Optional<String> getToken() {
        return token;
    }

    @Override
    public String getOrganization() {
        return "testOrganization";
    }

    @Override
    public void setOrganization(String organization) {
    }
}
