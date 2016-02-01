package fi.helsinki.cs.tmc.core.commands;

import static com.google.common.base.Strings.isNullOrEmpty;

import fi.helsinki.cs.tmc.core.communication.HttpResult;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.spyware.DiffSender;

import com.google.common.base.Optional;

import java.util.List;

/**
 * A {@link Command} for sending spyware data to the server.
 */
// TODO: Check from NB, rename at least diff to event
public class SendSpywareDiffs extends Command<List<HttpResult>> {

    private byte[] spywareDiffs;
    private DiffSender sender;

    /**
     * Constructs a send spyware diffs command using {@code settings} and {@code sender} for sending
     * {@code spywareDiffs} to the server.
     */
    public SendSpywareDiffs(TmcSettings settings, DiffSender sender, byte[] spywareDiffs) {
        super(settings);

        this.spywareDiffs = spywareDiffs;
        this.sender = sender;
    }

    // TODO: move validations?
    private void assertHasRequiredData() throws TmcCoreException {
        testUsername();
        testPassword();
        testCourse();
    }

    private void testCourse() throws TmcCoreException {
        Optional<Course> course = this.settings.getCurrentCourse();
        if (course == null || !course.isPresent()) {
            throw new TmcCoreException("No current course found from settings.");
        }
    }

    private void testPassword() throws TmcCoreException {
        if (isNullOrEmpty(settings.getPassword())) {
            throw new TmcCoreException("password must be set!");
        }
    }

    private void testUsername() throws TmcCoreException {
        if (isNullOrEmpty(settings.getUsername())) {
            throw new TmcCoreException("username must be set!");
        }
    }

    /**
     * Entry point for launching this command.
     */
    @Override
    public List<HttpResult> call() throws TmcCoreException {
        assertHasRequiredData();
        return this.sender.sendToSpyware(spywareDiffs, settings.getCurrentCourse().get());
    }
}
