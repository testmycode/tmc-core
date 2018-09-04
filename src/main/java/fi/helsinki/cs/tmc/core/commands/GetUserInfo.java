package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.UserInfo;

public class GetUserInfo extends Command<UserInfo> {

    public GetUserInfo(ProgressObserver observer) {
        super(observer);
    }

    @Override
    public UserInfo call() throws Exception {
        return tmcServerCommunicationTaskFactory.getUserInfo();
    }
}
