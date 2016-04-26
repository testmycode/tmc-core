package fi.helsinki.cs.tmc.core.exceptions;

public  class ObsoleteClientException extends UserVisibleException {
    // TODO: we'll need to make this message configurable per client.
    public ObsoleteClientException() {
        super("Please update the TMC plugin.\nUse Help -> Check for Updates.");
    }
}
