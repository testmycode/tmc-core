
package hy.tmc.core;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import hy.tmc.core.communication.CourseSubmitter;
import hy.tmc.core.communication.SubmissionPoller;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.submission.SubmissionResult;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.zipping.DefaultRootDetector;
import hy.tmc.core.zipping.ProjectRootFinder;
import hy.tmc.core.zipping.Zipper;
import java.io.IOException;

public class Main {
    
    public static void main(String[] args) throws IOException, TmcCoreException {
        TmcCore c = new TmcCore();
        ClientTmcSettings settings = new ClientTmcSettings();
        
        settings.setServerAddress("https://tmc.mooc.fi/staging");
        settings.setUsername("test");
        settings.setPassword("1234");
        
        UrlCommunicator urlComms = new UrlCommunicator(settings);
        TmcJsonParser jsonParser = new TmcJsonParser(urlComms, settings);
        Optional<Course> course = jsonParser.getCourse(19);
        settings.setCurrentCourse(course.or(new Course()));
        ListenableFuture<SubmissionResult> result = c.submit("/home/xtoxtox/NetBeansProjects/2014-mooc-no-deadline/viikko1-Viikko1_004.Muuttujat", settings);
        Futures.addCallback(result, new FutureCallback<SubmissionResult>(){

            @Override
            public void onSuccess(SubmissionResult v) {
                System.out.println("Resultin status: " + v.isAllTestsPassed());
                System.out.println("Resulti jotai ");
            }

            @Override
            public void onFailure(Throwable thrwbl) {
                System.out.println("Failure");
            }
            
        });
    }
}
