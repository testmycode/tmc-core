package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.domain.Organization;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import com.google.common.annotations.VisibleForTesting;

import java.util.List;

public class GetOrganizations extends Command<List<Organization>> {

    public GetOrganizations(ProgressObserver observer) {
        super(observer);
    }

    @VisibleForTesting
    GetOrganizations(ProgressObserver observer, TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
    }

    @Override
    public List<Organization> call() throws Exception {
        return tmcServerCommunicationTaskFactory.getOrganizationListTask();
    }
}
