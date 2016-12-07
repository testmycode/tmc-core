package fi.helsinki.cs.tmc.core.holders;

import fi.helsinki.cs.tmc.core.communication.oauth2.Oauth;
import fi.helsinki.cs.tmc.core.exceptions.UninitializedHolderException;

public final class TmcOauthHolder {

    private static Oauth oauth;

    private TmcOauthHolder() {
    }

    public static synchronized Oauth get() {
        if (TmcOauthHolder.oauth == null) {
            throw new UninitializedHolderException();
        }
        return TmcOauthHolder.oauth;
    }

    public static synchronized void set(Oauth oauth) {
        TmcOauthHolder.oauth = oauth;
    }
}
