package hy.tmc.core.zipping;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import java.io.File;

/**
 * Class for zipping stuff.
 */

public class Zipper implements ZipMaker {

    /**
     * Zips a single folder to destination.
     *
     * @param folderPath path to folder to unzip
     * @param outDestination path to unzip to
     * @throws ZipException if failed to create zip
     */
    @Override
    public void zip(String folderPath, String outDestination) throws ZipException {
        ZipFile zipFile = new ZipFile(new File(outDestination));
        zipFile.addFolder(folderPath, new ZipParameters());
    }
}
