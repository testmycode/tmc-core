package hy.tmc.cli.zipping;

import com.google.common.base.Optional;
import hy.tmc.cli.domain.Course;
import hy.tmc.core.exceptions.ProtocolException;
import java.io.IOException;
import java.nio.file.Path;

public interface RootFinder {

    Optional<Path> getRootDirectory(Path zipRoot);
    Optional<Course> getCurrentCourse(String path) throws IOException, ProtocolException;
}
