Feature: Help command
    User can list available commands.

    Scenario: List commands
        Given help command.
        Then output should contains commands.