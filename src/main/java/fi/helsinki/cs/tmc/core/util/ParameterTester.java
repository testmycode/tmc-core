package fi.helsinki.cs.tmc.core.util;

import com.google.common.base.Preconditions;
import static com.google.common.base.Strings.isNullOrEmpty;

import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

// WTF, why it exists????
public class ParameterTester {

    public static void checkStringParameters(String... params) throws TmcCoreException {
        for (String param : params) {
            Preconditions.checkArgument(isNullOrEmpty(param),"Param {" + param + "}empty or null.");
        }
    }
}
