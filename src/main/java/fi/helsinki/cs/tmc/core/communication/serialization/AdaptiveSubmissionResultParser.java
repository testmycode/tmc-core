package fi.helsinki.cs.tmc.core.communication.serialization;

import fi.helsinki.cs.tmc.core.domain.submission.AdaptiveSubmissionResult;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.testrunner.StackTraceSerializer;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdaptiveSubmissionResultParser{

    private static final Logger logger = LoggerFactory.getLogger(AdaptiveSubmissionResultParser.class);

    public AdaptiveSubmissionResult parseFromJson(final String json) {

        if (json.trim().isEmpty()) {
            logger.info("Attempted to parse empty string as JSON");
            throw new IllegalArgumentException("Empty input");
        }
        try {
            Gson gson =
                    new GsonBuilder()
                        .registerTypeAdapter(
                            SubmissionResult.Status.class, new SubmissionResultParser.StatusDeserializer())
                        .registerTypeAdapter(
                            StackTraceElement.class, new StackTraceSerializer())
                        .registerTypeAdapter(
                            ImmutableList.class, new SubmissionResultParser.ImmutableListJsonDeserializer())
                        .create();
            AdaptiveSubmissionResult result = gson.fromJson(json, AdaptiveSubmissionResult.class);
            return result;
            
        } catch (RuntimeException runtimeException) {
            logger.warn("Failed to parse adaptive submission result", runtimeException);
            throw new RuntimeException(
                "Failed to parse adaptive submission result: " + runtimeException.getMessage(),
                runtimeException);
        }
    }
}
