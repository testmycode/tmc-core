package fi.helsinki.cs.tmc.core.utilities;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Organization;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

public class TmcServerAddressNormalizer {

    public static void normalize() {
        TmcSettings tmcSettings = TmcSettingsHolder.get();
        String address = tmcSettings.getServerAddress();

        if (!address.contains("/org/") && address.contains("/hy")) {
            int last = address.lastIndexOf("/");
            tmcSettings.setServerAddress(address.substring(0, last));
            tmcSettings.setOrganization(address.substring(last + 1, address.length()));
        } else if (!address.contains("/org/")) {
            return;
        } else {
            String[] split = address.split("/org/");

            tmcSettings.setServerAddress(split[0]);

            String organization = split[1];
            if (organization.charAt(organization.length() - 1) == '/') {
                organization = organization.substring(0, organization.length());
            }
            tmcSettings.setOrganization(organization);
        }
    }
}
