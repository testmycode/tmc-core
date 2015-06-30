package hy.tmc.cli.frontend.communication.server;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import hy.tmc.cli.frontend.communication.commands.Command;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class SocketListener implements Runnable {

    private ListenableFuture<?> commandResult;
    private DataOutputStream output;
    private Socket socket;
    private Command command;

    public SocketListener(ListenableFuture<?> commandResult, DataOutputStream output, Socket socket, Command command) {
        this.commandResult = commandResult;
        this.output = output;
        this.socket = socket;
        this.command = command;
    }

    @Override
    public void run() {
        try {
            Object result = commandResult.get();
            @SuppressWarnings("unchecked")
            Optional<String> output = this.command.parseData(result);
            if (output.isPresent()) {
                writeToOutput(output.get());
            }
            this.command.cleanData();
        }
        catch (InterruptedException | ExecutionException | IOException ex) {
            System.err.println(Arrays.toString(ex.getStackTrace()));
            if (ex.getCause().getClass() == UnknownHostException.class) {
                writeToOutput("Unable to reach server: ");
            }
            writeToOutput(ex.getCause().getMessage());
            printLog(ex);
        }
    }

    /**
     * printLog prints exception to scripts/log.txt
     *
     * @param ex exception which can be any exception
     */
    private void printLog(Exception ex) {
        System.err.println(ex.getMessage() + "\n" + Arrays.toString(ex.getCause().getStackTrace()) + "\n" + Arrays.toString(ex.getStackTrace()));
    }

    private void writeToOutput(final String commandOutput) {
        try {
            output.write((commandOutput + "\n").getBytes());
            socket.close();
        }
        catch (IOException ex) {
            System.err.println("Failed to print error message: ");
            System.err.println(ex.getMessage());
        }
    }
}
