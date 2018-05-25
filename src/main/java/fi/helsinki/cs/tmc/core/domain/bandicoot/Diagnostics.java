package fi.helsinki.cs.tmc.core.domain.bandicoot;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.snapshots.HostInformationGenerator;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Locale;

public class Diagnostics implements Serializable {

    @SerializedName("java_vendor")
    private final String javaVendor;
    @SerializedName("java_version")
    private final String javaVersion;
    @SerializedName("client_name")
    private final String clientName;
    @SerializedName("client_version")
    private final String clientVersion;
    @SerializedName("host_program_name")
    private final String hostProgramName;
    @SerializedName("host_program_version")
    private final String hostProgramVersion;
    @SerializedName("os_arch")
    private final String osArch;
    @SerializedName("os_name")
    private final String osName;
    @SerializedName("os_version")
    private final String osVersion;
    @SerializedName("server_address")
    private final String serverAddress;
    private final Locale locale;
    @SerializedName("host_id")
    private String hostId;

    public Diagnostics() {
        TmcSettings settings = TmcSettingsHolder.get();
        this.javaVendor = System.getProperty("java.vendor");
        this.javaVersion = System.getProperty("java.version");
        this.clientName = settings.clientName();
        this.clientVersion = settings.clientVersion();
        this.hostProgramName = settings.hostProgramName();
        this.hostProgramVersion = settings.hostProgramVersion();
        this.osArch = System.getProperty("os.arch");
        this.osName = System.getProperty("os.name");
        this.osVersion = System.getProperty("os.version");
        this.locale = settings.getLocale();
        this.serverAddress = settings.getServerAddress();
        this.hostId = HostInformationGenerator.getHostId().substring(0, 1);
    }

    public String getJavaVersion() {
        return this.javaVersion;
    }
}
