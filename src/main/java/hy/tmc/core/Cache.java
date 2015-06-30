package hy.tmc.core;

import hy.tmc.core.domain.Course;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class Cache {
    
    private static Map<Integer, Course> courses;
    private static Date lastUpdated = new Date();
    
    public static void update(Map<Integer, Course> freshData) {
        lastUpdated = new Date();
        lastUpdated.setTime(System.currentTimeMillis());
    }
    
    public static Date getLastUpdated() {
        return lastUpdated;
    }
    
    public static void clear() {
        courses = new HashMap<>();
    }
    
    public static void loadFromDatabase() {
        // TODO: implement
    }
    
    public static void backupToDatabase() {
        // TODO: implement
    }
}
    
