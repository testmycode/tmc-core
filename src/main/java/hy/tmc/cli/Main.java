package hy.tmc.cli;

import hy.tmc.cli.frontend.FrontendListener;
import hy.tmc.cli.frontend.communication.server.ProtocolException;
import hy.tmc.cli.frontend.communication.server.Server;

import java.io.IOException;

public class Main {

    /**
     * Starts the main program.
     *
     * @param args arguments.
     * @throws java.io.IOException if there is an error while reading user input.
     * @throws java.lang.InterruptedException if server is interrupted. Starts the server.
     */
    public static void main(String[] args) throws IOException, InterruptedException, ProtocolException {
        FrontendListener frontendListener = new Server();
        frontendListener.start();
    }
}
