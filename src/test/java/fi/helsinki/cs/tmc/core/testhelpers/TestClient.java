package fi.helsinki.cs.tmc.core.testhelpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.Socket;

public class TestClient {

    private final int portnumber;

    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;

    public TestClient(int portnumber) throws IOException {
        this.portnumber = portnumber;
        this.init();
    }

    public void init() throws IOException {
        this.socket = new Socket("localhost", portnumber);
        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public boolean checkForMessages() throws IOException {
        return input.ready();
    }

    private boolean isClosedFromServer() {
        try {
            input.read();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public Socket getSocket() {
        return socket;
    }

    public void sendMessage(String message) throws IOException {
        output.println(message);
    }

    /**
     * Reads while socket is open.
     *
     * @return all lines read from the socket
     * @throws IOException if reading fails
     */
    public String getAllFromSocket() throws IOException {
        StringBuilder replybuffer = new StringBuilder();
        String reply = input.readLine();
        while (reply != null) {
            replybuffer.append(reply).append("\n");
            reply = input.readLine();
        }
        return replybuffer.toString();
    }

    /**
     * Reads one line from the socket.
     * Waits for input.
     *
     * @return last reply from frontend
     */
    public String reply() {
        try {
            String reply = input.readLine();
            if (reply == null) {
                this.init();
                return input.readLine();
            }
            return reply;
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            return "fail";
        }
    }

    public boolean isReadyToBeRead() throws IOException {
        return input.ready();
    }

    public boolean hasNewMessages() throws IOException {
        return input.ready();
    }
}
