package hy.tmc.core.zipping;

import java.io.File;
import java.nio.file.Path;


public class DefaultRootDetector implements ProjectRootDetector{
    
    /**
     * Determine whether a directory is a project root directory. The default
     * detector detects Maven and Ant projects. A directory will be considered
     * root if it contains a pom.xml or build.xml
     * 
     * @param path path of the directory
     * @return true iff the path denotes a directory which is a project root
     */
    @Override
    public boolean isRootDirectory(Path path) {
        File dir = path.toFile();
        if (!dir.isDirectory()) {
            return false;
        }
        for (File file : dir.listFiles()) {
            if (file.getName().equals("pom.xml")) {
                return true;
            }
            if (file.getName().equals("build.xml")) {
                return true;
            }
        }
        return false;
    }
}
