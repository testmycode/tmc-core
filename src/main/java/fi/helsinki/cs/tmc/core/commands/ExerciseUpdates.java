package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.LocalExerciseStatus;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by jamo on 14/06/16.
 */
public class ExerciseUpdates extends Command<Void> {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseUpdates.class);

    private final Course course;
    private final LocalExerciseStatus exerciseStatus;

    public ExerciseUpdates(ProgressObserver observer, Course course, LocalExerciseStatus exerciseStatus) {
        super(observer);
        Preconditions.checkNotNull(course);
        Preconditions.checkNotNull(exerciseStatus);
        this.course = course;
        this.exerciseStatus = exerciseStatus;
    }

    @VisibleForTesting
    ExerciseUpdates(
        ProgressObserver observer,
        TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory,
        Course course,
        LocalExerciseStatus exerciseStatus) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.course = course;
        this.exerciseStatus = exerciseStatus;
    }


    @Override
    public Void call() throws Exception {
        Course updatedCourse;
        try {
            updatedCourse = new GetCourseDetails(observer, course).call();
        } catch (Exception ex) {
            logger.warn("Failed to fetch exercises from server", ex);
            throw new TmcCoreException("Failed to fetch exercises from server", ex);
        }

        List<Exercise> updatedExercises = updatedCourse.getExercises();

        for (Exercise ex : updatedExercises) {
            if (!ex.hasDeadlinePassed()) {

                if (updatedCourse.getUnlockables().contains(ex)) {
                    // It's unlockable
                    //unlockable.add(ex);
                } else if (exerciseStatus.get && !ex.isLocked()) {
                    if (ex.isCompleted()) {
                        downloadableCompleted.add(ex);
                    } else {
                        downloadableUncompleted.add(ex);
                    }
                } else if (isDownloaded && projectMediator.isProjectOpen(proj)) {
                    open.add(ex);
                } else {
                    closed.add(ex); // TODO: all projects may end up here if this is queried too early
                }

                String downloadedChecksum = courseDb.getDownloadedExerciseChecksum(ex.getKey());
                if (isDownloaded && ObjectUtils.notEqual(downloadedChecksum, ex.getChecksum())) {
                    updateable.add(ex);
                }
            }



        return null;
    }
}
