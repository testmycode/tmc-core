package fi.helsinki.cs.tmc.core.domain;

public class ExerciseIdentifier {

    private final String courseName;
    private final String exerciseName;

    public ExerciseIdentifier(String courseName, String exerciseName) {
        this.courseName = courseName;
        this.exerciseName = exerciseName;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public boolean identifies(Exercise exercise) {
        return courseName.equals(exercise.getCourseName())
                && exerciseName.equals(exercise.getName());
    }
}
