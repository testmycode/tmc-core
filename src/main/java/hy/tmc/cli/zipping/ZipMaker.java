
package hy.tmc.cli.zipping;

import net.lingala.zip4j.exception.ZipException;

/**
 * Interface for stubbing zipper in tests.
 */
public interface ZipMaker {
    public void zip(String folderPath, String outDestination) throws ZipException;
}
