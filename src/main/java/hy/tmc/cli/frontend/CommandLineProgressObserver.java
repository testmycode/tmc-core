package hy.tmc.cli.frontend;

import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class CommandLineProgressObserver implements ProgressObserver {

    private DataOutputStream output;

    public CommandLineProgressObserver(DataOutputStream output) {
        this.output = output;
    }

    @Override
    public void progress(double completionPercentage, String message) {
        try {
            NumberFormat formatter = new DecimalFormat("#0.0");
            String percentage = formatter.format(completionPercentage);
            output.write((message + " (" + percentage + "% done)\n").getBytes());
        }
        catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

}
