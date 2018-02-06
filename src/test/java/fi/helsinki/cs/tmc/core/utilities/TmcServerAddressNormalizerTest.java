package fi.helsinki.cs.tmc.core.utilities;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Organization;
import fi.helsinki.cs.tmc.core.utils.MockSettings;

import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.concurrent.Callable;


public class TmcServerAddressNormalizerTest {

    @Mock
    TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory;
    TmcSettings settings;
    TmcServerAddressNormalizer normalizer;
    String baseAddress;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        this.settings = new MockSettings();
        this.normalizer = new TmcServerAddressNormalizer(this.settings, this.tmcServerCommunicationTaskFactory);
        this.baseAddress = "https://tmc.mooc.fi";
        try {
            doReturn(new Organization("Helsingin Yliopisto", "Helsingin Yliopisto", "hy", "/logo.png", false))
                    .when(this.tmcServerCommunicationTaskFactory).getOrganizationBySlug("hy");
        } catch (Exception e) {
        }
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (settings.getOrganization().isPresent()) {
                    return new Callable<Optional<Course>>() {
                        @Override
                        public Optional<Course> call() {
                            return Optional.of(new Course("Ohjelmoinnin Jatkokurssi"));
                        }
                    };
                } else {
                    return Optional.absent();
                }
            }
        }).when(this.tmcServerCommunicationTaskFactory).getCourseByIdTask(264);
    }

    @Test
    public void parsingWorksForRegularOrganizations() {
        String address = this.baseAddress + "/org/hy";
        this.settings.setServerAddress(address);
        normalize();
        assertEquals(true, this.settings.getOrganization().isPresent());
    }

    @Test
    public void parsingWorksForHySpecialCase() {
        String address = this.baseAddress + "/hy";
        this.settings.setServerAddress(address);
        normalize();
        assertEquals(true, this.settings.getOrganization().isPresent());
    }

    @Test
    public void parsingWorksForBaseServerAddress() {
        String address = this.baseAddress;
        this.settings.setServerAddress(address);
        normalize();
        assertEquals("https://tmc.mooc.fi", this.settings.getServerAddress());
    }

    @Test
    public void parsingWorksForCourses() {
        String address = this.baseAddress + "/org/hy/courses/264";
        this.settings.setServerAddress(address);
        normalize();
        assertEquals(true, this.settings.getOrganization().isPresent());
        assertEquals(true, this.settings.getCurrentCourse().isPresent());
    }

    @Test
    public void parsingFailsIfNoOrganizationButCoursePresent() {
        String address = this.baseAddress + "/courses/264";
        this.settings.setServerAddress(address);
        normalize();
        assertEquals(false, this.settings.getCurrentCourse().isPresent());
        assertEquals(false, this.settings.getOrganization().isPresent());
    }

    @Test
    public void parsingFailsIfAddressIsGibberish() {
        String address = "hurr durr herp derp";
        this.settings.setServerAddress(address);
        normalize();
        assertEquals(false, this.settings.getOrganization().isPresent());
        assertEquals(false, this.settings.getCurrentCourse().isPresent());
    }

    @Test
    public void parsingDoesNothingForOldServer() {
        String address = "https://tmc.mooc.fi/mooc";
        this.settings.setServerAddress(address);
        normalize();
        assertEquals("https://tmc.mooc.fi/mooc", this.settings.getServerAddress());
    }

    @Test
    public void parsingDoesNothingForOldServerWithCourse() {
        String address = "https://tmc.mooc.fi/mooc/courses/12";
        this.settings.setServerAddress(address);
        normalize();
        assertEquals("https://tmc.mooc.fi/mooc/courses/12", this.settings.getServerAddress());
    }

    private void normalize() {
        this.normalizer.normalize();
        this.normalizer.selectOrganizationAndCourse();
    }
}
