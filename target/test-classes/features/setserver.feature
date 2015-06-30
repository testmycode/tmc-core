Feature: ChooseServer command
    User can change the TMC-server

    Scenario: change server with correct param
        Given the server is "http://tmc.asdf.ffsd"
        When the user changes the server to "http://tmc.ebin.com"
        Then the server will be "http://tmc.ebin.com"
    
    Scenario: server not changed when no param given
        Given the server is "http://tmc.mooc.fi/staging"
        When the user uses the command without parameters
        Then the server is unchanged

    Scenario: the command does not accept every string
        Given the server is "http://tmc.mooc.fi/staging"
        When the user changes the server to "asdfasfasdf"
        Then the server is unchanged