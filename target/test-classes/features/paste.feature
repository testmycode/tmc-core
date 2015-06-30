Feature: Paste command
    User can submit exercise to TMC-server and recieve paste.

    Scenario: paste works from exercise folder
        Given user has logged in with username "test" and password "1234"
        When user gives command paste with valid path "/testResources/2013_ohpeJaOhja/viikko1" and exercise "Viikko1_002.HeiMaailma"
        And user executes the command
        Then user will see the paste url

    Scenario: paste will show mail in the mailbox
        Given the user has mail in the mailbox
        When user gives command paste with valid path "/testResources/2013_ohpeJaOhja/viikko1" and exercise "Viikko1_002.HeiMaailma"
        And user executes the command
        Then user will see the new mail

    Scenario: paste will start the polling
        Given polling for reviews is not in progress
        When user gives command paste with valid path "/testResources/2013_ohpeJaOhja/viikko1" and exercise "Viikko1_002.HeiMaailma"
        And user executes the command
        Then the polling will be started

     Scenario: paste works with vim too
        Given user has logged in with username "test" and password "1234"
        When user gives command paste with valid path "/testResources/2013_ohpeJaOhja/viikko1" and exercise "Viikko1_002.HeiMaailma"
        And flag "--vim"
        And user executes the command
        Then user will see the paste url
