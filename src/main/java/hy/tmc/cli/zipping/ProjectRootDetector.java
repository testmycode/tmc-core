package hy.tmc.cli.zipping;

import java.nio.file.Path;

public interface ProjectRootDetector {
    /**
     * Determine whether a directory is a project root directory.
     *
     * @param directory path of the directory
     * @return true iff the path denotes a directory which is a project root
     */
    public boolean isRootDirectory(Path directory);
}
