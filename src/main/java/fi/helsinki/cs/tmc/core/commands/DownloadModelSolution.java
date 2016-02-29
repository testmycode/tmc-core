package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

public class DownloadModelSolution extends Command<Void> {


    public DownloadModelSolution(ProgressObserver observer) {
        super(observer);
    }

    @Override
    public Void call() throws Exception {
        throw new UnsupportedOperationException("Not supported before CORE MILESTONE 3");
    }
}
