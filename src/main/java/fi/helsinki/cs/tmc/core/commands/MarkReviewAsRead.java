package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.Review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MarkReviewAsRead extends Command<Void>  {


    private static final Logger logger = LoggerFactory.getLogger(MarkReviewAsRead.class);

    private final Review review;

    public MarkReviewAsRead(ProgressObserver observer, Review review) {
        super(observer);
        this.review = review;
    }

    MarkReviewAsRead(
        ProgressObserver observer,
        Review review,
        TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.review = review;
    }

    @Override
    public Void call() throws Exception {
        logger.info("Marking review {} as read", review);
        tmcServerCommunicationTaskFactory.getMarkingReviewAsReadTask(review, true).call();
        return null;
    }
}
