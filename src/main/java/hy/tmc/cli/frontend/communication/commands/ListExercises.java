package hy.tmc.cli.frontend.communication.commands;

import com.google.common.base.Optional;
import hy.tmc.cli.backend.communication.ExerciseLister;
import hy.tmc.cli.configuration.ClientData;
import hy.tmc.cli.domain.Course;
import hy.tmc.cli.domain.Exercise;

import hy.tmc.core.exceptions.ProtocolException;
import hy.tmc.cli.synchronization.TmcServiceScheduler;
import java.io.IOException;

import java.util.List;

public class ListExercises extends Command<List<Exercise>> {

    private ExerciseLister lister;
    private Course current;
    private MailChecker mail;

    public ListExercises() {
        this(new ExerciseLister());
    }

    /**
     * For dependency injection for tests.
     *
     * @param lister mocked lister object.
     */
    public ListExercises(ExerciseLister lister) {
        mail = new MailChecker();
        this.lister = lister;
    }

    public ListExercises(String path) {
        this(new ExerciseLister());
        this.setParameter("path", path);
    }

    /**
     * Check the path and ClientData.
     *
     * @throws ProtocolException if some data not specified
     */
    @Override
    public void checkData() throws ProtocolException, IOException {
        if (!data.containsKey("path")) {
            throw new ProtocolException("Path not recieved");
        }
        if (!ClientData.userDataExists()) {
            throw new ProtocolException("Please authorize first.");
        }
        Optional<Course> currentCourse = ClientData.getCurrentCourse(data.get("path"));
        if (currentCourse.isPresent()) {
            this.current = currentCourse.get();
        } else {
            throw new ProtocolException("No course resolved from the path.");
        }
    }

    @Override
    public List<Exercise> call() throws ProtocolException, IOException {
        checkData();
        TmcServiceScheduler.startIfNotRunning(this.current);
        List<Exercise> exercises = lister.listExercises(data.get("path"));
        return exercises;
    }

    /**
     * HUOM EXTRAKTOI TÄMÄ OMAAN LUOKKAAN
     * Executes the mail command with necessary params.
     * Gives the mail command either a courseID (preferably) or a path
     * for determining which courses reviews and updates should be fetched.
     *
     * @throws ProtocolException if unable to find necessary params.
     */
    private String checkMail() throws IOException {
        if (data.containsKey("courseID")) {
            mail.setParameter("courseID", data.get("courseID"));
        } else if (data.containsKey("path")) {
            mail.setParameter("path", data.get("path"));
        } else {
            return "must specify path";
        }
        try {
            return mail.call();
        } catch (ProtocolException e) {
            return e.getMessage();
        }
    }
}
