package hy.tmc.core.testhelpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class Helper {

    private ProcessBuilder createProcessBuilder(String command, String cliPath) {
        if (cliPath == null) {
            cliPath = "scripts/frontend.sh";
        }
        return new ProcessBuilder("bash", cliPath, command);
    }

    private Process createProcess(String command, String cliPath, boolean waitUntilFinished) {
        Process pr = null;
        try {
            pr = createProcessBuilder(command, cliPath).start();
            if (waitUntilFinished) {
                pr.waitFor();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
        return pr;
    }

    public Process createAndStartProcess(String... params) throws IOException {
        return new ProcessBuilder(params).start();
    }

    public String printOutput(String command, String cliPath)
            throws InterruptedException, IOException {
        Process pr = createProcess(command, cliPath, true);
        return readOutputFromProcess(pr);
    }

    /**
     * Read output from process.
     */
    public String readOutputFromProcess(Process process) throws InterruptedException, IOException {
        process.waitFor();
        InputStream inputStream = process.getInputStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            return "";
        } finally {
            inputStream.close();
        }
        return sb.toString();
    }

    public Process startDialogWithCommand(String command, String cliPath) {
        Process process = createProcess(command, cliPath, false);
        waitMilliseconds(100);
        return process;
    }

    private void waitMilliseconds(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public Process writeInputToProcess(Process loginDialog, String input) throws IOException {
        OutputStream outputStream = loginDialog.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        try {
            writer.write(input);
            writer.newLine();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            writer.flush();
            writer.close(); // this could cause IOException BEWARE
        }
        waitMilliseconds(100);
        return loginDialog;
    }
}
