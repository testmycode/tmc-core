Feature: Logout
    User can logout.

    Scenario: Logout when logged in.
        Given logout command.
        When user sees message.
        Then user data should be cleared.
    Scenario: Logout when no user data is present.
        Given logout command without being logged in.
        When nothing should happen.
        Then user sees error message.