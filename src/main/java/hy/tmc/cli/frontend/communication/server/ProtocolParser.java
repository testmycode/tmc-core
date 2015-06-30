package hy.tmc.cli.frontend.communication.server;

import static hy.tmc.cli.frontend.communication.commands.CommandFactory.createCommandMap;

import hy.tmc.cli.frontend.FrontendListener;
import hy.tmc.cli.frontend.communication.commands.Command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProtocolParser parses user input to executable command.
 */
public class ProtocolParser {

    private FrontendListener server;

    /**
     * Constructor for Protocol Parser.
     *
     * @param server frontend server
     */
    public ProtocolParser(FrontendListener server) {
        this.server = server;
    }

    public ProtocolParser() {
    }

    /**
     * Constructor for Protocol Parser.
     *
     * @param server frontend server
     */
    public ProtocolParser(FrontendListener server, HashMap<String, Command> availableCommands) {
        this.server = server;
    }

    /**
     * Search for command by inputline.
     *
     * @param inputLine input String
     * @return Command that matches input
     * @throws ProtocolException if bad command name
     */
    public Command getCommand(String inputLine) throws ProtocolException {
        String[] elements = getElements(inputLine);
        String commandName = elements[0];
        Map<String, Command> commandsByName = createCommandMap();
        if (!commandsByName.containsKey(commandName)) {
            throw new ProtocolException("Invalid command name");
        }
        Command command = commandsByName.get(commandName);
        command = giveData(elements, command);
        return command;
    }

    private String[] getElements(String userInput) {
        List<String> items = new ArrayList<>();
        boolean parsingLongValue = false;
        String multiWordItem = "";
        for (String word : userInput.split(" ")) {
            if (parsingLongValue) {
                if (word.contains("}")) {
                    parsingLongValue = false;
                    items.add(multiWordItem.trim());
                    multiWordItem = "";
                } else {
                    multiWordItem += " " + word;
                }
            } else {
                if (word.contains("{")) {
                    parsingLongValue = true;
                } else {
                    items.add(word);
                }
            }
        }
        String[] array = new String[items.size()];
        array = items.toArray(array);
        return array;
    }

    private Command giveData(String[] userInput, Command command) {
        int index = 1;
        while (index < userInput.length) {
            String key = userInput[index];
            if (userInput[index].charAt(0) == '-') {
                command.setParameter(key, "");
                index++;
            } else {
                String value = userInput[index + 1].replace("<newline>", "\n");
                command.setParameter(key, value);
                index += 2;
            }
        }
        return command;
    }
}
