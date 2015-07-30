package hy.tmc.core.zipping;

import hy.tmc.core.zipping.DefaultUnzipDecider;
import hy.tmc.core.zipping.Unzipper;
import hy.tmc.core.zipping.UnzipDecider;
import hy.tmc.core.testhelpers.FileWriterHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class UnzipperTest {

    Unzipper handler;
    String v = File.separator;
    FileWriterHelper helper = new FileWriterHelper();
    String testZipPath = "testResources"+v+"test.zip";
    String unzipPath;
    UnzipDecider decider;
    private Path tempDir;
    String projectPath;
    String javaFile;

    public UnzipperTest() throws IOException {
        decider = new DefaultUnzipDecider();
        tempDir = Files.createTempDirectory(null);
        unzipPath = tempDir.toAbsolutePath().toString();
        projectPath = unzipPath + File.separator + "viikko1" + File.separator + "Viikko1_001.Nimi";
        javaFile = projectPath + File.separator + "src" + File.separator + "Nimi.java";
    }

    @Before
    public void setup() {
        handler = new Unzipper(testZipPath, unzipPath, decider);
    }

    /**
     * Deletes files used in tests.
     *
     * @throws java.io.IOException
     */
    @After
    public void teardown() throws IOException {
        final File file = new File(unzipPath);
        FileUtils.deleteDirectory(file);
        file.mkdir();
    }

    @Test
    public void findsFile() {
        assertTrue(new File(testZipPath).exists());
    }

    @Test
    public void oneFileUnzips() throws IOException, ZipException {
        handler.unzip();
        assertTrue(new File(unzipPath + File.separator + "viikko1").exists());
        assertTrue(new File(unzipPath + File.separator + "viikko1" + File.separator
                + "Viikko1_001.Nimi" + File.separator + "src").exists());
        assertTrue(new File(unzipPath + File.separator + "viikko1" + File.separator +
                "Viikko1_001.Nimi" + File.separator + "lib").exists());
    }

    @Test
    public void sourceFolderUnzips() throws IOException, ZipException {
        assertFalse(new File(javaFile).exists());
        handler.unzip();
        assertTrue(new File(javaFile).exists());
    }

    @Test
    public void sourceFolderDoesntOverride() throws ZipException, IOException {
        handler.unzip();
        File file = new File(javaFile);
        assertTrue(file.exists());
        helper.writeStuffToFile(javaFile);
        long modified = file.lastModified();
        handler.unzip();
        assertEquals(modified, file.lastModified());
    }

    @Test
    public void otherStuffIsOverwritten() throws IOException, ZipException {
        handler.unzip();
        File file = new File(unzipPath + File.separator + "viikko1" + File.separator +
                "Viikko1_001.Nimi" + File.separator + "build.xml");
        assertTrue(file.exists());
        helper.writeStuffToFile(file.getAbsolutePath());
        long modified = file.lastModified();
        handler.unzip();
        assertNotEquals(modified, file.lastModified());
    }

    @Test
    public void setFilePathSetsPath() {
        handler.setZipPath("best");
        assertEquals("best", handler.getZipPath());
    }

    @Test
    public void setUnzipPathSetsPath() {
        handler.setUnzipLocation("best");
        assertEquals("best", handler.getUnzipLocation());
    }

    @Test
    public void doesntUnzipBadPath() {
        try {
            handler.setZipPath("nonexistingplace");
            handler.unzip();
            fail("did not raise exception");
        } catch (IOException ex) {
            fail("Didn't work");
        } catch (net.lingala.zip4j.exception.ZipException ex) {
            //ok
        }
    }

    @Test
    public void doesntOverwriteSomethingInTmcprojectYml() throws IOException, ZipException {
        String studentTestFile = projectPath + File.separator + "test" + File.separator + "StudentTest.java";

        handler.unzip();

        helper.writeStuffToFile(studentTestFile);
        long lastMod = new File(studentTestFile).lastModified();
        handler.unzip();

        assertEquals(lastMod, new File(studentTestFile).lastModified());

    }

}
