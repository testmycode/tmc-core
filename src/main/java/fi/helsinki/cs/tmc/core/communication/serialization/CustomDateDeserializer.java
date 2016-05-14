package fi.helsinki.cs.tmc.core.communication.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class CustomDateDeserializer implements JsonDeserializer<Date> {

    private static final Logger logger = LoggerFactory.getLogger(CustomDateDeserializer.class);
    private static final SimpleDateFormat DATE_TIME_PARSER =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public Date deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
            throws JsonParseException {

        try {
            return DATE_TIME_PARSER.parse(je.getAsString());
        } catch (ParseException ex) {
            logger.warn("Failed to parse date", ex);
            throw new JsonParseException(ex);
        }
    }
}
