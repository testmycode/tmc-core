
package hy.tmc.cli.testhelpers;

import hy.tmc.cli.zipping.ZipMaker;
import net.lingala.zip4j.exception.ZipException;

public class ZipperStub implements ZipMaker{

    @Override
    public void zip(String folderPath, String outDestination) throws ZipException {
        
    }
    
}
