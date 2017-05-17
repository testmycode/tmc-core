package fi.helsinki.cs.tmc.core.commands;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Organization;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.utils.MockSettings;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.verify;

public class GetOrganizationsTest {
    @Mock
    ProgressObserver mockObserver;
    @Mock
    TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory;

    TmcSettings settings;
    private Command<List<Organization>> command;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        settings = new MockSettings();
        TmcSettingsHolder.set(settings);
        List<Organization> organizations = new ArrayList<Organization>();
        organizations.add(new Organization("test", "test", "test", "test", false));
        when(tmcServerCommunicationTaskFactory.getOrganizationListTask()).thenReturn(organizations);
    }

    @Test
    public void callsTmcServerCommunicationTaskFactoryGetOrganizationsList() throws Exception {
        command = new GetOrganizations(mockObserver, tmcServerCommunicationTaskFactory);
        command.call();
        verify(tmcServerCommunicationTaskFactory, times(1)).getOrganizationListTask();
    }
}
