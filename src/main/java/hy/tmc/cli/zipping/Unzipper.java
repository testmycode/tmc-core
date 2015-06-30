package hy.tmc.cli.zipping;

import java.io.File;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;

/**
 * Handles unpacking zip files downloaded from TMC.
 */
public class Unzipper {

    private String zipPath;
    private String unzipDestination;
    private UnzipDecider movedecider;
    private String specFileName = ".tmcproject.yml";

    /**
     * Creates ziphandler with specified zip path and unzip location.
     *
     * @param zipSourcePath for zip to unpack
     * @param unzipLocation place to unzip to
     * @param movedecider a class which helps decide which files may be
     * overwritten
     */
    public Unzipper(String zipSourcePath, String unzipLocation, UnzipDecider movedecider) {
        this.zipPath = zipSourcePath;
        this.unzipDestination = unzipLocation;
        this.movedecider = movedecider;
    }

    public String getUnzipLocation() {
        return unzipDestination;
    }

    public void setUnzipLocation(String unzipDestination) {
        this.unzipDestination = unzipDestination;
    }

    public String getZipPath() {
        return zipPath;
    }

    public void setZipPath(String zipPath) {
        this.zipPath = zipPath;
    }

    /**
     * Usage of generics <?> is because TMC-langs returns generic List-
     * implementation. This makes the typecast much safer.
     */
    private void extractYml(ZipFile zipFile) throws ZipException {
        List<?> fileHeaders = (List<?>) zipFile.getFileHeaders();
        for (Object object : fileHeaders) {
            FileHeader fileHeader = (FileHeader) object;
            if (fileHeader.getFileName().endsWith(specFileName)) {
                zipFile.extractFile(fileHeader, unzipDestination);
                Path tmcYmlPath = Paths.get(unzipDestination + File.separator + fileHeader.getFileName());
                this.movedecider.readTmcprojectYml(tmcYmlPath);
            }
        }
    }

    /**
     * Unzips zip to specified location. Uses generics because TMC-langs.
     *
     * @throws IOException if cannot write to file
     * @throws ZipException If specified zip is not found
     */
    public void unzip() throws IOException, ZipException {

        ZipFile zipFile = new ZipFile(zipPath);
        extractYml(zipFile);

        List<?> fileHeaders = (List<?>) zipFile.getFileHeaders();

        for (Object object : fileHeaders) {
            FileHeader fileHeader = (FileHeader) object;
            String fullFileName = unzipDestination + File.separator + fileHeader.getFileName();
            if (movedecider.canBeOverwritten(fullFileName)) {
                zipFile.extractFile(fileHeader, unzipDestination);
            }
        }
    }
}
