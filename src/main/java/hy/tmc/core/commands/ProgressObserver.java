package hy.tmc.core.commands;

public interface ProgressObserver {
    
    void progress(double completionPercentage, String message);
    
}
