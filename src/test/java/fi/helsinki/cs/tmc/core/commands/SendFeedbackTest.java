package fi.helsinki.cs.tmc.core.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackAnswer;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackQuestion;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;

public class SendFeedbackTest {

    @Rule public TemporaryFolder testFolder = new TemporaryFolder();

    @Mock ProgressObserver mockObserver;
    @Mock TmcSettings settings;
    @Mock TmcServerCommunicationTaskFactory factory;
    @Mock Course mockCourse;
    @Mock Exercise mockExercise;

    private static final URI FEEDBACK_URI = URI.create("http://example.com/feedback");
    private static final List<FeedbackAnswer> ANSWER_LIST =
            ImmutableList.of(new FeedbackAnswer(new FeedbackQuestion()));
    private Command<Boolean> command;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TmcSettingsHolder.set(settings);

        command = new SendFeedback(mockObserver, ANSWER_LIST, FEEDBACK_URI, factory);
    }

    @Test
    public void testHappyPath() throws Exception {
        verifyZeroInteractions(mockObserver);
        when(factory.getFeedbackAnsweringJob(any(URI.class), any(List.class)))
                .thenReturn(
                        new Callable<String>() {
                            @Override
                            public String call() throws Exception {
                                return "{\"status\": \"ok\"}";
                            }
                        });
        assertTrue(command.call());
    }

    @Test
    public void testSadPath() throws Exception {
        verifyZeroInteractions(mockObserver);
        when(factory.getFeedbackAnsweringJob(any(URI.class), any(List.class)))
                .thenReturn(
                        new Callable<String>() {
                            @Override
                            public String call() throws Exception {
                                return "{\"status\": \"fail\"}";
                            }
                        });
        assertFalse(command.call());
    }
}
