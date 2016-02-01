package fi.helsinki.cs.tmc.core.communication;

// TODO: why exist
public class HttpResult {

    private String data;
    private int statusCode;
    private boolean success;

    /**
     * Creates a new HttpResult object to model the result of some request.
     *
     * @param data data returned by the server
     * @param statusCode statuscode of the request e.g. 200
     * @param success was the request succesful
     */
    public HttpResult(String data, int statusCode, boolean success) {
        this.data = data;
        this.statusCode = statusCode;
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
