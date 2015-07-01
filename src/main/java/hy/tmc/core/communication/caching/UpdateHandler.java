package hy.tmc.core.communication.caching;

import com.google.gson.Gson;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

public class UpdateHandler extends NotificationHandler<Exercise> {
    
    private File cache;
    
    public UpdateHandler(File cacheFile) {
        super();
        this.cache = cacheFile;
    }

    @Override
    protected List<Exercise> fetchFromServer(Course currentCourse) throws IOException {
        List<Exercise> exercises = TmcJsonParser.getExercises(currentCourse);
        if (exercises == null) {
            return new ArrayList<>();
        }
        return exercises;
    }

    @Override
    protected boolean isNew(Exercise exercise) {
        return false;
    }

     protected List<Exercise> readFromFile() throws FileNotFoundException, IOException {
        String json = FileUtils.readFileToString(cache, Charset.forName("UTF-8"));
        List<Exercise> objects = new Gson().fromJson(json, List.class);
        if (objects == null) {
            return new ArrayList<>();
        }
        return objects;
    }

    protected List<Exercise> readAndDeleteFromFile() throws IOException {
        List<Exercise> objects = this.readFromFile();
        this.clearFile();
        return objects;
    }

    protected void clearFile() throws IOException {
        this.cache.delete();
        this.cache.createNewFile();
    }

    protected void save(List<Exercise> reviews) throws FileNotFoundException {
        String cacheJson = new Gson().toJson(reviews);
        try (PrintWriter writer = new PrintWriter(this.cache)) {
            writer.write(cacheJson);
        }
    }
    
}
