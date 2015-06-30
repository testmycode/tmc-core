package hy.tmc.cli.frontend.communication.commands;

import com.google.common.base.Joiner;

import com.google.common.base.Optional;

public class Help extends Command<String> {

    /**
     * Does nothing, this command does not require data.
     */
    @Override
    public void checkData() {
    }

    @Override
    public String call() {
        return helpMessage();
    }

    /**
     * Takes the command map and returns a set of command names.
     *
     * @return a set of all available command names.
     */
    public String helpMessage() {
        StringBuilder enterprise = new StringBuilder();

        enterprise.append("Available commands:\n")
                .append("help\n")
                .append("test\n")
                .append("submit\n")
                .append("paste\n")
                .append("list exercises\n")
                .append("list courses\n")
                .append("download <course ID>\n")
                .append("set server <tmc-server address>\n")
                .append("login\n")
                .append("logout");

        return enterprise.toString();
    }
}
