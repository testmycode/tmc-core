package fi.helsinki.cs.tmc.core.holders;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.exceptions.UninitializedHolderException;

public final class TmcSettingsHolder {

    private static TmcSettings settings;

    private TmcSettingsHolder() {}

    public static synchronized TmcSettings get() {
        if (settings == null) {
            throw new UninitializedHolderException();
        }
        return settings;
    }

    public static synchronized void set(TmcSettings settings) {
        TmcSettingsHolder.settings = settings;
    }
}
