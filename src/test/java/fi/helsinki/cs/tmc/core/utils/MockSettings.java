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
    private String serverAddress;
    private Optional<Course> selected;
    private Optional<Organization> org;

    public MockSettings() {
        this.token = Optional.absent();
        this.serverAddress = "testAddress";
        this.selected = Optional.<Course>absent();
        this.org = Optional.<Organization>absent();
    }

    @Override
    public String getServerAddress() {
        return this.serverAddress;
    }

    @Override
    public void setServerAddress(String address) {
        this.serverAddress = address;
    }

    @Override
    public Optional<String> getPassword() {
        return Optional.absent();
    }

    @Override
    public void setPassword(Optional<String> password) {

    }

    @Override
    public Optional<Integer> getId() {
        return Optional.absent();
    }

    @Override
    public void setId(int id) {

    }

    @Override
    public Optional<String> getUsername() {
        return Optional.of("testUsername");
    }

    @Override
    public void setUsername(String username) {

    }

    @Override
    public Optional<String> getEmail() {
        return Optional.absent();
    }

    @Override
    public void setEmail(String email) {

    }

    @Override
    public boolean userDataExists() {
        return false;
    }

    @Override
    public Optional<Course> getCurrentCourse() {
        return selected;
    }

    @Override
    public String clientName() {
        return "testClient";
    }

    @Override
    public String clientVersion() {
        return "testVersion";
    }

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
    public void setCourse(Optional<Course> course) {
        this.selected = course;
    }

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
    public Optional<OauthCredentials> getOauthCredentials() {
        OauthCredentials credentials = new OauthCredentials();
        credentials.setOauthApplicationId("testOauthApplicationId");
        credentials.setOauthSecret("testOauthSecret");
        return Optional.of(credentials);
    }

    @Override
    public void setOauthCredentials(Optional<OauthCredentials> credentials) {

    }

    @Override
    public void setToken(Optional<String> token) {
        this.token = token;
    }

    @Override
    public Optional<String> getToken() {
        return token;
    }

    @Override
    public Optional<Organization> getOrganization() {
        return this.org;
    }

    @Override
    public void setOrganization(Optional<Organization> organization) {
        this.org = organization;
    }

}
