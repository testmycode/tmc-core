package fi.helsinki.cs.tmc.core.exceptions;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class FailedHttpResponseException extends Exception {
    private final int statusCode;
    private final HttpEntity entity;

    private FailedHttpResponseException(int statusCode, HttpEntity entity, String message) {
        super(message);
        this.statusCode = statusCode;
        this.entity = entity;
    }

    public static FailedHttpResponseException fromResponse(int statusCode, HttpEntity entity) {
        if (statusCode / 100 == 5) {
            return new FailedHttpResponseException(statusCode, entity,
                    "There was an internal error on the server, please try again later. Response code: " + statusCode);
        }
        return new FailedHttpResponseException(statusCode, entity, "Response code: " + statusCode);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public HttpEntity getEntity() {
        return entity;
    }

    public String getEntityAsString() {
        try {
            return EntityUtils.toString(entity, "UTF-8");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
