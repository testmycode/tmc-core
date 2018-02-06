package fi.helsinki.cs.tmc.core.utilities;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Organization;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import java.io.IOException;

public class TmcServerAddressNormalizer {

    private TmcSettings tmcSettings;
    private TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory;
    private String organizationSlug;
    private int courseId;

    public TmcServerAddressNormalizer() {
        this.tmcSettings = TmcSettingsHolder.get();
        this.tmcServerCommunicationTaskFactory = new TmcServerCommunicationTaskFactory();
        this.organizationSlug = "";
        this.courseId = -1;
    }

    @VisibleForTesting
    TmcServerAddressNormalizer(TmcSettings settings, TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        this.tmcSettings = settings;
        this.tmcServerCommunicationTaskFactory = tmcServerCommunicationTaskFactory;
        this.organizationSlug = "";
        this.courseId = -1;
    }

    public void normalize() {
        parseCourseAndOrganizationFromAddress();
    }

    public void selectOrganizationAndCourse() {
        if (!this.organizationSlug.isEmpty()) {
            try {
                Optional<Organization> org = Optional.of(this.tmcServerCommunicationTaskFactory.getOrganizationBySlug(this.organizationSlug));
                if (org.isPresent()) {
                    this.tmcSettings.setOrganization(org);
                }
            } catch (IOException e) {
            } catch (Exception e) {
            }
        }

        if (this.courseId != -1) {
            try {
                Optional<Course> selected = this.tmcServerCommunicationTaskFactory.getCourseByIdTask(this.courseId).call();
                if (selected.isPresent()) {
                    this.tmcSettings.setCourse(selected);
                }
            } catch (Exception e) {
            }
        }
    }

    private void parseCourseAndOrganizationFromAddress() {
        if (this.tmcSettings.getServerAddress().contains("https://tmc.mooc.fi/mooc")) {
            return;
        }

        String serverAddress = this.tmcSettings.getServerAddress();
        if (serverAddress.contains("/courses/")) {
            String[] split = serverAddress.split("/courses/");

            parseOrganizationFromAddress(split[0]);

            if (split[1].endsWith("/")) {
                split[1] = split[1].substring(0, split[1].length() - 1);
            }

            this.courseId = Integer.parseInt(split[1]);
        } else {
            parseOrganizationFromAddress(serverAddress);
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
