package fi.helsinki.cs.tmc.core.holders;

import fi.helsinki.cs.tmc.core.exceptions.UninitializedHolderException;
import fi.helsinki.cs.tmc.core.persistance.TmcState;

public class TmcStateHolder {

    private static TmcState tmcState;

    private TmcStateHolder() {}

    public static synchronized TmcState get() {
        if (tmcState == null) {
            throw new UninitializedHolderException();
        }
        return tmcState;
    }

    public static synchronized void set(TmcState tmcState) {
        TmcStateHolder.tmcState = tmcState;
    }
}
