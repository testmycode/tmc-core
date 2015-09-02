package fi.helsinki.cs.tmc.core.util;

import com.google.common.base.Optional;
import java.util.Collection;

public class Optionals {

    public static <T, I extends Collection<T>> Optional<I> ofNonEmpty(I collection) {
        if (collection.isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(collection);
    }
}
