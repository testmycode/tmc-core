package hy.tmc.core.zipping;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultUnzipDecider implements UnzipDecider {

    protected List<String> additionalStudentFiles;
    private String studentFilesKey = "extra_student_files";

    /**
     * Default movedecider, which decides whether something can be overwritten in a Maven or an Ant
     * project.
     */
    public DefaultUnzipDecider() {
        this.additionalStudentFiles = new ArrayList<>();
    }

    /**
     * Decide whether this file should be moved or not, that is, does it contain work by students.
     * Studentfiles are all files in the source directory, in this case src or src/main. Also files
     * specified by .tmcproject.yml in the project root are studentfiles
     *
     * @param path path of the file
     * @return true iff this file should be overwritten
     */
    @Override
    public boolean canBeOverwritten(String path) {
        for (String unOverwritable : this.additionalStudentFiles) {
            if (path.endsWith(unOverwritable)) {
                return false;
            }
        }
        return !(path.contains(File.separator + "src" + File.separator) && new File(path).exists());
    }

    /**
     * Find and read .tmcproject.yml. The ziphandler will invoke this method. SupressWarnings
     * required for xlint.
     *
     * @param ymlFilePath path that the project has been unzipped to initially
     */
    @Override
    public void readTmcprojectYml(Path ymlFilePath) {
        String contents;
        try {
            contents = FileUtils.readFileToString(ymlFilePath.toFile());
        }
        catch (IOException ex) {
            return;
        }
        Yaml yaml = new Yaml();
        @SuppressWarnings("unchecked")
        Map<String, List<String>> map = (Map<String, List<String>>) yaml.load(contents);
        if (map.containsKey(studentFilesKey)) {
            this.additionalStudentFiles = map.get(studentFilesKey);
        }
    }
}
