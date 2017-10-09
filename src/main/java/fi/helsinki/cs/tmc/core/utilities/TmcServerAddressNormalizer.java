package fi.helsinki.cs.tmc.core.utilities;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Organization;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import com.google.common.base.Optional;

import java.io.IOException;

public class TmcServerAddressNormalizer {

    public static void normalize() {
        TmcSettings tmcSettings = TmcSettingsHolder.get();
        String address = tmcSettings.getServerAddress();

        TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory = new TmcServerCommunicationTaskFactory();
        if (!address.contains("/org/") && address.contains("/hy")) {
            int last = address.lastIndexOf("/");
            tmcSettings.setServerAddress(address.substring(0, last));
            try {
                tmcSettings.setOrganization(
                        Optional.fromNullable(tmcServerCommunicationTaskFactory.getOrganizationBySlug(address.substring(last + 1, address.length()))));
            } catch (IOException e) {
            }
        } else if (!address.contains("/org/")) {
            return;
        } else {
            String[] split = address.split("/org/");

            tmcSettings.setServerAddress(split[0]);

            String organization = split[1];
            Optional<Organization> org = Optional.<Organization>absent();
            if (organization.charAt(organization.length() - 1) == '/') {
                organization = organization.substring(0, organization.length());
                try {
                    org = Optional.of(tmcServerCommunicationTaskFactory.getOrganizationBySlug(organization.substring(0, organization.length())));
                } catch (IOException e) {
                }
            }
            tmcSettings.setOrganization(org);
        }
    }
}
