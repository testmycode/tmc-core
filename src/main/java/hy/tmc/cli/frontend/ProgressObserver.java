package hy.tmc.cli.frontend;

public interface ProgressObserver {
    
    void progress(double completionPercentage, String message);
    
}
