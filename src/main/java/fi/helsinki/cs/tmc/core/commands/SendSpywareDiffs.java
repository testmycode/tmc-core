package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.HttpResult;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.spyware.DiffSender;

import com.google.common.base.Optional;

import java.util.List;

public class SendSpywareDiffs extends Command<List<HttpResult>> {

    private byte[] spywareDiffs;
    private Course currentCourse;
    private DiffSender sender;

    public SendSpywareDiffs(
            byte[] spywareDiffs,
            DiffSender sender,
            TmcSettings settings) {
        super(settings);

        this.spywareDiffs = spywareDiffs;
        this.sender = sender;
    }

    private void assertHasRequiredData() throws TmcCoreException {
        String username = settings.getUsername();
        if (username == null || username.isEmpty()) {
            throw new TmcCoreException("username must be set!");
        }

        String password = settings.getPassword();
        if (password == null || password.isEmpty()) {
            throw new TmcCoreException("password must be set!");
        }

        Optional<Course> course = this.settings.getCurrentCourse();
        if (course != null && course.isPresent()) {
            this.currentCourse = course.get();
        } else {
            throw new TmcCoreException("No current course found from settings.");
        }
    }

    @Override
    public List<HttpResult> call() throws Exception {
        assertHasRequiredData();
        return this.sender.sendToSpyware(spywareDiffs, currentCourse);
    }
}
