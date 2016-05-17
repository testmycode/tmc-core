package fi.helsinki.cs.tmc.core.utilities;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.apache.commons.codec.binary.Base64;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * Converts JsonMaker to byte[] to base64 in JSON.
 */
public class JsonMakerGsonSerializer
        implements JsonSerializer<JsonMaker> /*, JsonDeserializer<JsonMaker> */ {
    @Override
    public JsonElement serialize(JsonMaker data, Type type, JsonSerializationContext jsc) {

        if (data == null) {
            return JsonNull.INSTANCE;
        } else {
            return new JsonPrimitive(
                    Base64.encodeBase64String(data.toString().getBytes(Charset.forName("UTF-8"))));
        }
    }

    //TODO: figure out how to decerialize
}
