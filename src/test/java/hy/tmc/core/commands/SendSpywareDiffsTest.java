package hy.tmc.core.commands;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertFalse;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import hy.tmc.core.CoreTestSettings;
import hy.tmc.core.TmcCore;
import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.TmcCoreException;
import hy.tmc.core.testhelpers.ExampleJson;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SendSpywareDiffsTest {

    private TmcCore core;

    @Rule public WireMockRule wireMock = new WireMockRule();

    @Before
    public void setup() {
        this.core = new TmcCore();
    }

    @Test(expected = TmcCoreException.class)
    public void testCheckDataNoDiff() throws Exception {
        CoreTestSettings settings = new CoreTestSettings();
        settings.setUsername("snapshotTest");
        settings.setPassword("snapshotTest");
        this.core.sendSpywareDiffs(null, settings);
    }

    @Test(expected = TmcCoreException.class)
    public void testCheckDataNoUsername() throws Exception {
        CoreTestSettings settings = new CoreTestSettings();
        settings.setPassword("snapshotTest");
        this.core.sendSpywareDiffs(new byte[5000], settings);
    }

    @Test
    public void testCall() throws Exception {
        wireMock.stubFor(
                get(urlEqualTo("/staging.spyware.testmycode.net/"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody("SPYWARE TULI PERILLE")));

        CoreTestSettings settings = setupSettings();
        byte[] diffs = new byte[] {1, 4, 6};
        final List<HttpResult> result = new ArrayList<HttpResult>();
        ListenableFuture<List<HttpResult>> sendFuture = this.core.sendSpywareDiffs(diffs, settings);
        Futures.addCallback(
                sendFuture,
                new FutureCallback<List<HttpResult>>() {

                    @Override
                    public void onSuccess(List<HttpResult> results) {
                        for (HttpResult res : results) {
                            result.add(res);
                        }
                    }

                    @Override
                    public void onFailure(Throwable thrwbl) {
                        System.err.println("virhe: " + thrwbl);
                    }
                });
        while (!sendFuture.isDone()) {
            Thread.sleep(100);
        }
        assertFalse(result.isEmpty());
    }

    private CoreTestSettings setupSettings() {
        CoreTestSettings settings = new CoreTestSettings();
        TmcJsonParser parser = new TmcJsonParser(settings);
        List<Course> courses = parser.getCoursesFromString(ExampleJson.allCoursesExample);
        Course currentCourse = courses.get(1);
        settings.setCurrentCourse(currentCourse);
        settings.setUsername("snapshotNelja");
        settings.setPassword("snapshotNelja");
        return settings;
    }
}
