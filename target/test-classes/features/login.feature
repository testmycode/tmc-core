Feature: Authentication
    User can login.

    Scenario: Login.
        When user gives username "test" and password "1234"
        Then user should see result.