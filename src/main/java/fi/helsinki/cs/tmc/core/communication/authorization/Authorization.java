package fi.helsinki.cs.tmc.core.communication.authorization;

import org.apache.commons.codec.binary.Base64;

// TODO: why own package?
public class Authorization {

    public static String encode(String data) {
        return Base64.encodeBase64String(data.getBytes());
    }
}
