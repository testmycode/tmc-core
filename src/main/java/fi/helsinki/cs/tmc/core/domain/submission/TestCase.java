package fi.helsinki.cs.tmc.core.domain.submission;

import com.google.gson.annotations.SerializedName;

// Replace from langs or langs-abstraction
public class TestCase {

    private boolean successful;
    private String name;
    private String message;
    private TestException exception;

    @SerializedName("detailed_message")
    private String detailedMessage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TestException getException() {
        return exception;
    }

    public void setException(TestException exception) {
        this.exception = exception;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public void setDetailedMessage(String detailedMessage) {
        this.detailedMessage = detailedMessage;
    }
}
