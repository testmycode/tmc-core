package fi.helsinki.cs.tmc.core.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class AdaptiveExercise extends Exercise implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(AdaptiveExercise.class);

    private boolean available;

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
