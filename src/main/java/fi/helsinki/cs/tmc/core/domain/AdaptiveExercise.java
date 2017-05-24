package fi.helsinki.cs.tmc.core.domain;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdaptiveExercise extends Exercise implements Serializable {
    
    private static final Logger logger = LoggerFactory.getLogger(AdaptiveExercise.class);
    //private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    
    private boolean available;
    
    public Boolean getAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
}
