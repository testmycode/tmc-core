package fi.helsinki.cs.tmc.core.holders;

import fi.helsinki.cs.tmc.core.communication.oauth2.Oauth;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.exceptions.UninitializedHolderException;

import com.google.common.base.Optional;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TmcSettingsHolder {

    private static TmcSettings settings;

    private static final Logger logger = LoggerFactory.getLogger(TmcSettingsHolder.class);

    private TmcSettingsHolder() {}

    public static synchronized TmcSettings get() {
        if (TmcSettingsHolder.settings == null) {
            throw new UninitializedHolderException();
        }
        return TmcSettingsHolder.settings;
    }

    public static synchronized void set(TmcSettings settings) {
        TmcSettingsHolder.settings = settings;
        if (settings != null) {
            migrateOldSettings();
        }
    }
    // TODO delete at some point
    private static void migrateOldSettings() {
        try {
            Optional<String> password = settings.getPassword();
            if (password.isPresent()) {
                Oauth.getInstance().fetchNewToken(password.get());
                settings.setPassword(Optional.<String>absent());
            }
        } catch (OAuthSystemException | OAuthProblemException e) {
            logger.warn("Settings migration failed.");
        }
    }
}
