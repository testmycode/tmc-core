package hy.tmc.core.commands;


import hy.tmc.core.commands.MailChecker;
import hy.tmc.core.Mailbox;
import hy.tmc.core.configuration.ClientData;
import hy.tmc.core.exceptions.ProtocolException;
import hy.tmc.core.testhelpers.MailExample;
import hy.tmc.core.testhelpers.ProjectRootFinderStub;
import java.io.IOException;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.junit.Assert.assertTrue;

public class MailCheckerTest {

    private MailChecker mailChecker;

    @Before
    public void before() throws Exception {
        Mailbox.create();
        Mailbox.getMailbox().get().fill(MailExample.reviewExample());
        this.mailChecker = new MailChecker();
        ClientData.setProjectRootFinder(new ProjectRootFinderStub());

    }

    @After
    public void after() throws Exception {
        Mailbox.destroy();
    }

    @Test(expected = ProtocolException.class)
    public void notValidIfNoPathSpecified() throws Exception {
        mailChecker.checkData();
    }

    @Test(expected = ProtocolException.class)
    public void notValidIfNoMailbox() throws Exception {
        Mailbox.destroy();
        mailChecker.data.put("path", "asd");
        mailChecker.checkData();
    }

    @Test(expected = ProtocolException.class)
    public void notValidIfNotLoggedIn() throws Exception {
        ClientData.clearUserData();
        mailChecker.data.put("courseID", "3");
        mailChecker.checkData();
    }

    @Test(expected = ProtocolException.class)
    public void notValidIfPathIsWeird() throws Exception {
        mailChecker.data.put("path", "asd");
        ClientData.setUserData("samu", "bossman");
        mailChecker.checkData();
    }
} 
