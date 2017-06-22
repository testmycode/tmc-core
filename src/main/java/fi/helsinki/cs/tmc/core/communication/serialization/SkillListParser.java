package fi.helsinki.cs.tmc.core.communication.serialization;

import fi.helsinki.cs.tmc.core.domain.Skill;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by markovai on 16.6.2017.
 */
public class SkillListParser {
    private static final Logger logger = LoggerFactory.getLogger(SkillListParser.class);

    public List<Skill> parseFromJson(String json) {
        if (json == null) {
            throw new NullPointerException("Json string is null");
        }
        if (json.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty input");
        }
        try {
            Gson gson =
                    new GsonBuilder()
                        .registerTypeAdapter(Date.class, new CustomDateDeserializer())
                        .create();

            Skill[] skills = gson.fromJson(json, Skill[].class);
            return Arrays.asList(skills);
        } catch (RuntimeException ex) {
            logger.warn("Failed to parse weeks info", ex);
            throw new RuntimeException("Failed to parse adaptive week list: " + ex.getMessage(), ex);
        }
    }
}
