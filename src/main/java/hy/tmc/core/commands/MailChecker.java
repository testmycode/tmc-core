package hy.tmc.core.commands;

import com.google.common.base.Optional;

import hy.tmc.core.Mailbox;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.ProtocolException;
import java.io.IOException;

public class MailChecker extends Command<String> {

    private Optional<Mailbox> mailbox;
    private Optional<Course> course;

    public MailChecker() {
        mailbox = Mailbox.getMailbox();
    }

    @Override
    public void checkData() throws ProtocolException, IOException {
        if (!ClientData.userDataExists()) {
            throw new ProtocolException("Must be logged in first");
        }
        mailbox = Mailbox.getMailbox();
        if (!mailbox.isPresent()) {
            throw new ProtocolException("No mailbox found. Are you logged in?");
        }
        if (data.containsKey("courseID")) {
            try {
                course = TmcJsonParser.getCourse(Integer.parseInt(data.get("courseID")));
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        } else if (data.containsKey("path")) {
            String path = data.get("path");
            course = ClientData.getCurrentCourse(path);
        } else {
            throw new ProtocolException("must specify path or courseID");
        }
        if (!course.isPresent()) {
            String errorMsg = "Unable to determine the course. Are you sure this is a tmc course subdirectory?";
            throw new ProtocolException(errorMsg);
        }
    }

    @Override
    public String call() throws ProtocolException, IOException {
        checkData();
        String mail = "";
        if (mailbox.get().reviewsWaiting()) {
            mailbox.get().getUnreadReviews();
        }
        return mail;
    }
}
