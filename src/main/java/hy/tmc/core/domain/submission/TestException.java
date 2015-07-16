package hy.tmc.core.domain.submission;


public class TestException {

    private String className;
    private String message;
    private StackTrace[] stackTrace;
    private String cause;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMessage() {
        return message;
    }

    public String getCause() {
        return this.cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public StackTrace[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTrace[] stackTrace) {
        this.stackTrace = stackTrace;
    }
}
