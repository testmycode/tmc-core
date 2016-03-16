package fi.helsinki.cs.tmc.core.communication.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * Converts byte[] to/from base64 in JSON.
 */
public class ByteArrayGsonSerializer
        implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

    private static final Logger logger = LoggerFactory.getLogger(ByteArrayGsonSerializer.class);

    @Override
    public JsonElement serialize(byte[] data, Type type, JsonSerializationContext jsc) {
        if (data == null) {
            return JsonNull.INSTANCE;
        } else {
            return new JsonPrimitive(Base64.encodeBase64String(data));
        }
    }

    @Override
    public byte[] deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
            throws JsonParseException {
        if (je.isJsonPrimitive() && ((JsonPrimitive)je).isString()) {
            return Base64.decodeBase64(je.getAsString());
        } else if (je.isJsonNull()) {
            return null;
        } else {
            logger.warn("Byte array deserialization failed: not a base 64 string");
            throw new JsonParseException("Not a base64 string.");
        }
    }
}
