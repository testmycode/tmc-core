Feature: students may give feedback on the exercises

    Scenario: Feedback will not be asked when the exercise is not complete
        Given an exercise where some tests fail
        When the exercise is submitted
        Then feedback questions will not be asked

    Scenario: The students feedback is sent to the server
        Given the user has submitted a successful exercise
        When the user has answered all feedback questions
        Then feedback is sent to the server successfully

    Scenario: Feedback is sent even if some answers are not in the correct range
        Given the user has submitted a successful exercise
        When the user gives some answer that's not in the correct range
        Then feedback is sent to the server successfully

    Scenario: If there are no feedback questions, the program does not try to ask any
        Given an exercise with no feedback
        When the user submits
        Then feedback questions will not be asked