package hy.tmc.core.zipping;

import hy.tmc.core.zipping.Zipper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZipperTest {

    Zipper zipper;
    String mockPath = "testResources/mockProject";
    String mockUnzipPath = "testResources/mockProject.zip";

    @Before
    public void setUp() {
        zipper = new Zipper();
    }

    @After
    public void tearDown() {
        new File(mockUnzipPath).delete();
    }

    @Test
    public void zipsFolder() throws ZipException {
        zipper.zip(mockPath, mockUnzipPath);
        ZipFile file = new ZipFile(mockUnzipPath);
        List<?> filesInZip = (List<?>) file.getFileHeaders();
        List<String> names = new ArrayList<>();

        for (Object object : filesInZip) {
            FileHeader header = (FileHeader) object;
            names.add(header.getFileName());
        }
        assertEquals(5, names.size());
        assertTrue(names.contains("mockProject/root/.tmcproject.yml"));
        assertTrue(names.contains("mockProject/root/"));
    }

    @Test
    public void overridesPreviousExistingZip() throws IOException, ZipException {
        String newFileName = "best.txt";

        zipper.zip(mockPath, mockUnzipPath);

        ZipFile zip = new ZipFile(mockUnzipPath);
        File newFile = new File(newFileName);
        newFile.createNewFile();
        zip.addFile(newFile, new ZipParameters());

        assertEquals(newFileName, zip.getFileHeader(newFileName).getFileName());
        assertTrue(new File(mockUnzipPath).exists());

        zipper.zip(mockPath, mockUnzipPath);
        zip = new ZipFile(mockUnzipPath);
        FileHeader header = zip.getFileHeader(newFileName);
        new File(newFileName).delete();
        assertEquals(newFileName, header.getFileName());
    }

}
