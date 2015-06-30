package hy.tmc.cli.frontend.communication.commands;

import com.google.common.base.Strings;
import hy.tmc.cli.configuration.ConfigHandler;
import hy.tmc.core.exceptions.ProtocolException;
import java.io.IOException;
import java.util.regex.Pattern;

public class ChooseServer extends Command<Boolean> {

    private ConfigHandler handler;

    public ChooseServer(ConfigHandler handler) {
        this.handler = handler;
    }

    public ChooseServer() {
        handler = new ConfigHandler();
    }

    public ChooseServer(String serverAddress) {
        handler = new ConfigHandler();
        this.setParameter("tmc-server", serverAddress);
    }

    @Override
    public void checkData() throws ProtocolException {
        if (!this.data.containsKey("tmc-server")) {
            throw new ProtocolException("must specify new server");
        }
        if (!isValidTmcUrl(this.data.get("tmc-server"))) {
            throw new ProtocolException("given URL is not valid");
        }
    }

    private boolean isValidTmcUrl(String url) {
        String urlPattern = "(https?://)?([a-z]+\\.){2,}[a-z]+(/[a-z]+)*";
        Pattern tmcServerAddress = Pattern.compile(urlPattern);
        if (Strings.isNullOrEmpty(url)) {
            return false;
        }
        return tmcServerAddress.matcher(url).matches();
    }

    @Override
    public Boolean call() throws ProtocolException {

        String address = data.get("tmc-server");
        try {
            handler.writeServerAddress(address);
            return true;
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
}
