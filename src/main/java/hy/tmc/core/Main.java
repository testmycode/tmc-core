
package hy.tmc.core;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import hy.tmc.core.commands.Submit;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.submission.SubmissionResult;
import hy.tmc.core.exceptions.TmcCoreException;
import java.io.IOException;

public class Main {
    
    public static void main(String[] args) throws Exception{
        TmcCore c = new TmcCore();
        ClientTmcSettings settings = new ClientTmcSettings();
        
        settings.setServerAddress("https://tmc.mooc.fi/staging");
        settings.setUsername("test");
        settings.setPassword("1234");
        
        UrlCommunicator urlComms = new UrlCommunicator(settings);
        TmcJsonParser jsonParser = new TmcJsonParser(urlComms, settings);
        Optional<Course> course = jsonParser.getCourse(19);
        settings.setCurrentCourse(course.or(new Course()));
        String path = "/home/samutamm/NetBeansProjects/2014-mooc-no-deadline/viikko1/Viikko1_001.Nimi/src";
        Submit submit = new Submit(path, settings);
        SubmissionResult result = submit.call();
        System.out.println("submissionResult: " + result);
    }
}
