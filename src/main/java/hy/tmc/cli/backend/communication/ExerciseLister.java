package hy.tmc.cli.backend.communication;

import com.google.common.base.Optional;
import hy.tmc.cli.domain.Course;
import hy.tmc.cli.domain.Exercise;
import static hy.tmc.cli.frontend.ColorFormatter.coloredString;
import hy.tmc.cli.frontend.CommandLineColor;
import static hy.tmc.cli.frontend.CommandLineColor.GREEN;
import static hy.tmc.cli.frontend.CommandLineColor.RED;
import static hy.tmc.cli.frontend.CommandLineColor.WHITE;
import hy.tmc.cli.frontend.communication.server.ProtocolException;
import hy.tmc.cli.zipping.DefaultRootDetector;
import hy.tmc.cli.zipping.ProjectRootFinder;
import hy.tmc.cli.zipping.RootFinder;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class ExerciseLister {

    private RootFinder finder;

    /**
     * Default Constructor with default root finder.
     */
    public ExerciseLister() {
        finder = new ProjectRootFinder(new DefaultRootDetector());
    }

    /**
     * Constructor with specific finder.
     *
     * @param finder a RootFinder instance.
     */
    public ExerciseLister(RootFinder finder) {
        this.finder = finder;
    }

    /**
     * Returns a list of exercises of a current directory in which a course exists.
     *
     * @param path directory path to lookup course from
     * @return String with a list of exercises.
     */
    public List<Exercise> listExercises(String path) throws ProtocolException, IOException {
        Optional<Course> course = finder.getCurrentCourse(path);

        if (!course.isPresent()) {
            throw new ProtocolException("No course found");
        }

        List<Exercise> exercises = TmcJsonParser.getExercises(course.get());
        if (exercises == null || exercises.isEmpty()) {
            throw new ProtocolException("No exercises found");
        }

        return exercises;
    }

    /**
     * Builds a printout of the exercises given.
     *
     * @param exercises to build info from
     * @return String containing information
     */
    public String buildExercisesInfo(List<Exercise> exercises) {
        StringBuilder builder = new StringBuilder();

        for (Exercise exercise : exercises) {
            builder.append(buildSuccessOrFailMessage(exercise))
                    .append(exercise.getName())
                    .append("\n");

        }
        builder.append(endSummary(exercises));
        return builder.toString();
    }

    private String buildSuccessOrFailMessage(Exercise exercise) {
        if (exercise.isCompleted()) {
            return coloredString("[x] ", CommandLineColor.GREEN);
        } else if (exercise.isAttempted()) {
            return coloredString("[ ] ", CommandLineColor.RED);
        } else {
            return coloredString("[ ] ", CommandLineColor.WHITE);
        }
    }

    private String endSummary(List<Exercise> exercises) {
        int completed = 0;
        int attempted = 0;
        int total = 0;

        for (Exercise exercise : exercises) {
            if (exercise.isCompleted()) {
                completed++;
            } else if (exercise.isAttempted()) {
                attempted++;
            }
            total++;
        }
        return coloredString("Completed: " + completed + percentage(completed, total), GREEN) + " "
                + coloredString("Attempted: " + attempted + percentage(attempted, total), RED) + " "
                + coloredString("Total: " + total, WHITE);
    }

    private String percentage(int amount, int total) {
        double percentage = 100.0 * amount / total;
        NumberFormat formatter = new DecimalFormat("#0.0");
        
        return " (" + formatter.format(percentage) +"%)";
    }
}
