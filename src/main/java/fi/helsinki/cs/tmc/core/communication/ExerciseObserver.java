
package fi.helsinki.cs.tmc.core.communication;

import fi.helsinki.cs.tmc.core.domain.Exercise;

public interface ExerciseObserver {
    ExerciseObserver NOP = new ExerciseObserver() {
        @Override
        public void observe(Exercise exercise, boolean success) {
        }
    };
    void observe(Exercise exercise, boolean success) ;
}
