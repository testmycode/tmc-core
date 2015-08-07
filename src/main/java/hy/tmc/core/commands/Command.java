package hy.tmc.core.commands;

import hy.tmc.core.communication.UrlHelper;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.ProgressObserver;
import hy.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class Command<E> implements Callable<E> {

    protected Map<String, String> data;
    private String defaultErrorMessage = "Unexpected exception.";
    protected TmcSettings settings;
    protected ProgressObserver observer;

    /**
     * Command can return any type of object. For example SubmissionResult etc.
     */
    public Command() {
        data = new HashMap<>();
    }

    public Command(TmcSettings settings) {
        this();
        this.settings = settings;
    }

    public Command(ProgressObserver observer, TmcSettings settings) {
        this(settings);
        this.observer = observer;
    }

    public Map<String, String> getData() {
        return data;
    }

    /**
     * setParameter sets parameter data for command.
     *
     * @param key name of the datum
     * @param value value of the datum
     */
    public void setParameter(String key, String value) {
        data.put(key, value);
    }

    /**
     * Command must have checkData method which throws ProtocolException if it doesn't have all data
     * needed.
     *
     * @throws TmcCoreException if the command lacks some necessary data
     */
    public abstract void checkData() throws TmcCoreException, IOException;

    public void cleanData() {
        data.clear();
    }

    public void setObserver(ProgressObserver observer) {
        this.observer = observer;
    }

    public boolean settingsNotPresent() {
        return this.settings == null
                || !this.settings.userDataExists()
                || !serverAddressIsValid();
    }

    private boolean serverAddressIsValid() {
        if (this.settings.getServerAddress() == null ||
            this.settings.getServerAddress().isEmpty()) {
            return false;
        }
        return new UrlHelper(settings).urlIsValid(this.settings.getServerAddress());
    }
}
