package fi.helsinki.cs.tmc.core.communication.serialization;

import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.stylerunner.validation.CheckstyleResult;
import fi.helsinki.cs.tmc.testrunner.StackTraceSerializer;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class SubmissionResultParser {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionResultParser.class);

    public SubmissionResult parseFromJson(final String json) {

        if (json.trim().isEmpty()) {
            logger.info("Attempted to parse empty string as JSON");
            throw new IllegalArgumentException("Empty input");
        }

        try {

            Gson gson =
                    new GsonBuilder()
                            .registerTypeAdapter(
                                    SubmissionResult.Status.class, new StatusDeserializer())
                            // TODO: is this needed anymore?
                            .registerTypeAdapter(
                                    StackTraceElement.class, new StackTraceSerializer())
                            .registerTypeAdapter(ImmutableList.class, new ImmutableListJsonDeserializer())
                            .create();

            SubmissionResult result = gson.fromJson(json, SubmissionResult.class);

            // Parse validations field from JSON
            JsonObject output = new JsonParser().parse(json).getAsJsonObject();
            JsonElement validationElement = output.get("validations");

            if (validationElement != null) {
                result.setValidationResult(CheckstyleResult.build(validationElement.toString()));
            } else {
                result.setValidationResult(CheckstyleResult.build("{}"));
            }

            return result;

        } catch (RuntimeException | IOException runtimeException) {
            logger.warn("Failed to parse submission result", runtimeException);
            throw new RuntimeException(
                    "Failed to parse submission result: " + runtimeException.getMessage(),
                    runtimeException);
        }
    }

    private static class StatusDeserializer implements JsonDeserializer<SubmissionResult.Status> {

        private static final Logger logger = LoggerFactory.getLogger(StatusDeserializer.class);

        @Override
        public SubmissionResult.Status deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            String str = json.getAsJsonPrimitive().getAsString();
            try {
                return SubmissionResult.Status.valueOf(str.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Attempted to parse unknown submission status " + str);
                throw new JsonParseException("Unknown submission status: " + str);
            }
        }
    }

    private class ImmutableListJsonDeserializer implements JsonDeserializer<ImmutableList<?>> {
        @Override
        public ImmutableList<?> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            final Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
            final Type parametrizedType = listOf(typeArguments[0]).getType();
            final List<?> list = context.deserialize(json, parametrizedType);
            return ImmutableList.copyOf(list);
        }

        private <E> TypeToken<List<E>> listOf(final Type arg) {
            return new TypeToken<List<E>>() {}
                .where(new TypeParameter<E>() {}, (TypeToken<E>) TypeToken.of(arg));
        }
    }
}
