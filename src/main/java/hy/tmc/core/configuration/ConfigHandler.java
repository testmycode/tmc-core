package hy.tmc.core.configuration;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Writes data to config file and reads from it.
 */
public class ConfigHandler {

    public static final String apiVersion = "7";

    private String configFilePath;
    private String portFieldName = "serverPort";
    private String serverAddressFieldName = "serverAddress";
    public final String apiParam = "api_version=" + apiVersion;
    public final String coursesExtension = "/courses.json?" + apiParam;
    public final String authExtension = "/user";

    /**
     * Creates new config handler with default filename and path in current
     * directory.
     */
    public ConfigHandler() {
        this.configFilePath = "config.properties";
    }

    /**
     * Creates new config handler with specified path and name.
     *
     * @param path for config file
     */
    public ConfigHandler(String path) {
        this.configFilePath = path;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFileName) {
        this.configFilePath = configFileName;
    }

    private Properties getProperties() {
        Properties prop = new Properties();
        try {
            InputStream inputStream = new FileInputStream(new File(configFilePath));
            prop.load(inputStream);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return prop;
    }

    private void writeData(String property, String data) throws IOException {
        Properties prop = getProperties();
        prop.setProperty(property, data);
        FileWriter writer = new FileWriter(new File(configFilePath));
        prop.store(writer, "Updated properties");
        writer.close();
    }

    /**
     * Writes server address to config file, ex. "https://tmc.mooc.fi/hy".
     * @param address for tmc server
     * @throws IOException if unable to write address
     */
    public void writeServerAddress(String address) throws IOException {
        writeData(serverAddressFieldName, address);
    }

    /**
     * Reads and returns the server address of the TMC-server.
     * @return address of tmc server
     */
    public String readServerAddress() {
        Properties prop = getProperties();
        return prop.getProperty(serverAddressFieldName);
    }
    /**
     * Reads address from which to list courses.
     *
     * @return String with tmc server address + courses path
     */
    public String readCoursesAddress() {
        String serverAddress = readServerAddress();
        if (isNullOrEmpty(serverAddress)) {
            return null;
        }
        return serverAddress + coursesExtension;
    }

    /**
     * Reads address to which auth GET can be sent.
     *
     * @return String with tmc server address + user path
     */
    public String readAuthAddress() {
        String serverAddress = readServerAddress();
        if (isNullOrEmpty(serverAddress)) {
            return null;
        }
        return serverAddress + authExtension;
    }

    /**
     * Returns an complete URL to course's json feed in
     * defined server.
     * @param id course id
     * @return complete url to course json
     */
    public String getCourseUrl(int id) {
        return this.readServerAddress() + "/courses/"
                + id + ".json"
                + "?api_version=" + apiVersion;
    }

    /**
     * Reads port local server from config file.
     */
    public int readPort() {
        Properties prop = getProperties();
        return Integer.parseInt(prop.getProperty(portFieldName));
    }

    /**
     * Writes port to config file.
     *
     * @param port to write in config
     */
    public void writePort(int port) throws IOException {
        writeData(portFieldName, Integer.toString(port));
    }

    public String getAuthExtension() {
        return authExtension;
    }
}
