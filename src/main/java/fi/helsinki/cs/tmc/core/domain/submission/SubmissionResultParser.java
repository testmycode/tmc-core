package fi.helsinki.cs.tmc.core.domain.submission;

import fi.helsinki.cs.tmc.stylerunner.validation.CheckstyleResult;
import fi.helsinki.cs.tmc.testrunner.StackTraceSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.lang.reflect.Type;

public class SubmissionResultParser {

    public SubmissionResult parseFromJson(final String json) {

        if (json.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty input");
        }

        try {
            SubmissionResult result = getSubmissionResult(json);

            // Parse validations field from JSON
            JsonElement validationElement = getJsonElement(json);

            if (validationElement != null) {
                CheckstyleResult checkstyleResult =
                        CheckstyleResult.build(validationElement.toString());
                result.setValidationResult(checkstyleResult);
            } else {
                result.setValidationResult(CheckstyleResult.build("{}"));
            }

            return result;

        } catch (RuntimeException | IOException exception) {
            throw new RuntimeException(
                    "Failed to parse submission result: " + exception.getMessage(), exception);
        }
    }

    private JsonElement getJsonElement(String json) {
        JsonObject output = new JsonParser().parse(json).getAsJsonObject();
        return output.get("validations");
    }

    private SubmissionResult getSubmissionResult(String json) {
        Gson gson =
                new GsonBuilder()
                        .registerTypeAdapter(
                                SubmissionResult.Status.class, new StatusDeserializer())
                        .registerTypeAdapter(StackTraceElement.class, new StackTraceSerializer())
                        .create();

        return gson.fromJson(json, SubmissionResult.class);
    }

    private static class StatusDeserializer implements JsonDeserializer<SubmissionResult.Status> {
        @Override
        public SubmissionResult.Status deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            String string = json.getAsJsonPrimitive().getAsString();
            try {
                return SubmissionResult.Status.valueOf(string.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Unknown submission status: " + string);
            }
        }
    }
}
