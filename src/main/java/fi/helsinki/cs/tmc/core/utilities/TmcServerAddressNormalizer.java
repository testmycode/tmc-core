package fi.helsinki.cs.tmc.core.utilities;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Organization;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import com.google.common.base.Optional;

import java.io.IOException;

public class TmcServerAddressNormalizer {

    private TmcSettings tmcSettings;
    private String address;
    private TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory;
    private String organizationSlug;
    private int courseId;

    public TmcServerAddressNormalizer() {
        this.tmcSettings = TmcSettingsHolder.get();
        this.address = this.tmcSettings.getServerAddress();
        this.tmcServerCommunicationTaskFactory = new TmcServerCommunicationTaskFactory();
        this.organizationSlug = "";
        this.courseId = -1;
    }

    public void normalize() {
        parseCourseAndOrganizationFromAddress();
    }

    public void selectOrganizationAndCourse() {
        Optional<Organization> org = Optional.<Organization>absent();
        try {
            org = Optional.of(this.tmcServerCommunicationTaskFactory.getOrganizationBySlug(this.organizationSlug));
        } catch (IOException e) {
        }
        this.tmcSettings.setOrganization(org);

        Optional<Course> selected = Optional.<Course>absent();
        try {
            selected = this.tmcServerCommunicationTaskFactory.getCourseByIdTask(this.courseId).call();
        } catch (Exception e) {
        }
        this.tmcSettings.setCourse(selected);
    }

    private void parseCourseAndOrganizationFromAddress() {
        if (this.address.contains("/courses/")) {
            String[] split = this.address.split("/courses/");

            parseOrganizationFromAddress(split[0]);

            if (split[1].endsWith("/")) {
                split[1] = split[1].substring(0, split[1].length() - 1);
            }

            this.courseId = Integer.parseInt(split[1]);
        } else {
            parseOrganizationFromAddress(this.address);
        }
    }

    private void parseOrganizationFromAddress(String address) {
        String baseAddress = address;
        if (address.contains("/org/")) {
            String[] split = address.split("/org/");
            baseAddress = split[0];
            this.organizationSlug = split[1];
        } else if (!address.contains("/org/") && address.contains("/hy")) {
            int last = address.lastIndexOf("/");
            baseAddress = address.substring(0, last);
            this.organizationSlug = address.substring(last + 1, address.length());
        }
        this.tmcSettings.setServerAddress(baseAddress);
    }
}
