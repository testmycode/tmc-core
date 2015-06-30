package hy.tmc.core.zipping;

import java.nio.file.Path;

public interface UnzipDecider {

    /**
     * Decide whether this file should be moved or not, that is, does it contain
     * work by students. Studentfiles are all files in the source directory,
     * which will be different for different kinds of projects. Also files 
     * specified by .tmcproject.yml in the project root are studentfiles.
     *
     * @param path path of the file
     * @return true iff this file should be overwritten
     */
    public boolean canBeOverwritten(String path);
    
    /**
     * Find and read .tmcproject.yml. The ziphandler will invoke this method
     *
     * @param zipRoot path that the project has been unzipped to initially
     */
    public void readTmcprojectYml(Path zipRoot);
}
