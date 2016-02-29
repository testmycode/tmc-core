package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

public class DownloadCompletedExercises extends Command<Void> {


    public DownloadCompletedExercises(ProgressObserver observer) {
        super(observer);
    }

    @Override
    public Void call() throws Exception {
        throw new UnsupportedOperationException("Not support before CORE MILESTONE 2");
    }
}
