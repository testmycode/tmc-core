package hy.tmc.cli.frontend.communication.commands;

import com.google.common.base.Optional;

public class ReplyToPing extends Command<String> {

    private final String answer = "pong";

    /**
     * Does nothing, this command requires no data.
     */
    @Override
    public void checkData() {
    }

    @Override
    public String call() {
        return answer;
    }

    @Override
    public Optional<String> parseData(Object data) {
        String response = (String) data;
        return Optional.of(response);
    }
}
