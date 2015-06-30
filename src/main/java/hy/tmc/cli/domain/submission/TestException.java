package hy.tmc.cli.domain.submission;


public class TestException {

    private String className;
    private String message;
    private StackTrace[] stackTrace;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMessage() {
        return message;
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
