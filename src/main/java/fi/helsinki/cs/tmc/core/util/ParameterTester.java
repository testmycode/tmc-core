package fi.helsinki.cs.tmc.core.util;

import static com.google.common.base.Strings.isNullOrEmpty;

import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

public class ParameterTester {

    public static void checkStringParameters(String...params) throws TmcCoreException {
        for (String param : params) {
            if (isNullOrEmpty(param)) {
                throw new TmcCoreException("Param {" + param + "}empty or null.");
            }
        }
    }
}
