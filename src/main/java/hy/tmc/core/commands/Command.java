package hy.tmc.core.commands;

import com.google.common.base.Optional;
import hy.tmc.core.exceptions.TmcCoreException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class Command<E> implements Callable<E> {

    protected Map<String, String> data;
    private String defaultErrorMessage = "Unexpected exception.";
    protected ProgressObserver observer;

    /**
     * Command can return any type of object. For example SubmissionResult etc.
     */
    public Command() {
        data = new HashMap<>();
    }
    
    public Command(ProgressObserver observer) {
        this();
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
     * Command must have checkData method which throws ProtocolException if it
     * doesn't have all data needed.
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

}
