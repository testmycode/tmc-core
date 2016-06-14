package fi.helsinki.cs.tmc.core.domain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

/**
 * Transmits clients state of exercises to TMC-Core for filtering of new exercises updates etc.
 */
public class LocalExerciseStatus {

    private List<Exercise> open = Lists.newArrayList();
    private List<Exercise> closed = Lists.newArrayList();
    private List<Exercise> downloadableUncompleted = Lists.newArrayList();
    private List<Exercise> downloadableCompleted = Lists.newArrayList();
    private List<Exercise> updateable = Lists.newArrayList();
    private List<Exercise> unlockable = Lists.newArrayList();

    public ImmutableList<Exercise> getOpen() {
        return ImmutableList.copyOf(open);
    }

    public ImmutableList<Exercise> getClosed() {
        return ImmutableList.copyOf(closed);
    }

    public ImmutableList<Exercise> getDownloadableUncompleted() {
        return ImmutableList.copyOf(downloadableUncompleted);
    }

    public ImmutableList<Exercise> getDownloadableCompleted() {
        return ImmutableList.copyOf(downloadableCompleted);
    }

    public ImmutableList<Exercise> getUpdateable() {
        return ImmutableList.copyOf(updateable);
    }

    public ImmutableList<Exercise> getUnlockable() {
        return ImmutableList.copyOf(unlockable);
    }

    public LocalExerciseStatus addClosed(Exercise closed) {
        this.closed.add(closed);
        return this;
    }

    public LocalExerciseStatus addClosed(Collection<? extends Exercise> closed) {
        this.closed.addAll(closed);
        return this;
    }

    public LocalExerciseStatus addDownloadableUncompleted(Exercise downloadableUncompleted) {
        this.downloadableUncompleted.add(downloadableUncompleted);
        return this;
    }

    public LocalExerciseStatus addDownloadableUncompleted(Collection<? extends Exercise> downloadableUncompleted) {
        this.downloadableUncompleted.addAll(downloadableUncompleted);
        return this;
    }

    public LocalExerciseStatus addDownloadableCompleted(Exercise  downloadableCompleted) {
        this.downloadableCompleted.add(downloadableCompleted);
        return this;
    }

    public LocalExerciseStatus addDownloadableCompleted(Collection<? extends Exercise> downloadableCompleted) {
        this.downloadableCompleted.addAll(downloadableCompleted);
        return this;
    }

    public LocalExerciseStatus addUpdateable(Exercise updateable) {
        this.updateable.add(updateable);
        return this;
    }

    public LocalExerciseStatus addUpdateable(Collection<? extends Exercise>  updateable) {
        this.updateable.addAll(updateable);
        return this;
    }

    public LocalExerciseStatus addUnlockable(Exercise unlockable) {
        this.unlockable.add(unlockable);
        return this;
    }

    public LocalExerciseStatus addUnlockable(Collection<? extends Exercise> unlockable) {
        this.unlockable.addAll(unlockable);
        return this;
    }

    public LocalExerciseStatus addOpen(Exercise open) {
        this.open.add(open);
        return this;
    }

    public LocalExerciseStatus addOpen(Collection<? extends Exercise> open) {
        this.open.addAll(open);
        return this;
    }
}
