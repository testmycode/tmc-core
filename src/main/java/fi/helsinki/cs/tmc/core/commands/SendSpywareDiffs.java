package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.HttpResult;
import fi.helsinki.cs.tmc.core.communication.TmcJsonParser;
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
    private TmcJsonParser jsonParser;

    /**
     * Standard constructor.
     */
    public SendSpywareDiffs(byte[] spywereDiffs, TmcSettings settings) {
        this(spywereDiffs, new DiffSender(settings), new TmcJsonParser(settings), settings);
    }

    /**
     * Dependecy injection for tests.
     */
    public SendSpywareDiffs(
            byte[] spywareDiffs,
            DiffSender sender,
            TmcJsonParser jsonParser,
            TmcSettings settings) {
        super(settings);
        this.spywareDiffs = spywareDiffs;
        this.sender = sender;
        this.jsonParser = jsonParser;
    }

    @Override
    public void checkData() throws TmcCoreException {
        if (this.spywareDiffs == null) {
            throw new TmcCoreException("No spyware-diff given.");
        }
        if (this.settings.getUsername() == null || this.settings.getPassword() == null) {
            throw new TmcCoreException("No username/password defined.");
        }
        Optional<Course> course = this.settings.getCurrentCourse();
        if (course.isPresent()) {
            this.currentCourse = course.get();
        } else {
            throw new TmcCoreException("No current course found from settings.");
        }
    }

    @Override
    public List<HttpResult> call() throws Exception {
        checkData();
        return this.sender.sendToSpyware(spywareDiffs, currentCourse);
    }
}
