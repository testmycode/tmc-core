package hy.tmc.core.commands;

import hy.tmc.core.CoreTestSettings;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.exceptions.TmcCoreException;
import java.io.IOException;
import org.junit.Test;
import org.junit.Before;

public class CommandTest {
   
    CoreTestSettings settings = new CoreTestSettings();

    @Before
    public void setup() {
        settings.setCurrentCourse(new Course());
        settings.setPassword("asldjasd");
        settings.setUsername("sakljf");
    }
    
    @Test(expected = TmcCoreException.class)
    public void testSettingsNotPresent() throws Exception{
        testServerValidating("https.//tmc.mooc.fi/staging"); 
    }
    
    @Test(expected = TmcCoreException.class)
    public void testSettingsNotPresent2() throws Exception{
        testServerValidating("https:/tmc.mooc.fi/staging"); 
    }
    
    @Test(expected = TmcCoreException.class)
    public void testSettingsNotPresent3() throws Exception{
        testServerValidating("http://tmc.mooc.fi/staging"); 
    }
    
    @Test
    public void testSettingsArePresent() throws Exception{
        testServerValidating("https://tmc.mooc.fi/staging");
        testServerValidating("https://tmc.mooc.fi/mooc");
        testServerValidating("https://tmc.mooc.fi/hy");
    }
    
    private void testServerValidating(String address) throws TmcCoreException, IOException {
        settings.setServerAddress(address); //see TYPO
        CommandImpl ci = new CommandImpl(settings);
        ci.checkData();
    }

    public class CommandImpl extends Command {

        public CommandImpl(TmcSettings settings) {
            super(settings);
        }

        public void checkData() throws TmcCoreException, IOException {
            if (settingsNotPresent()) {
                throw new TmcCoreException();
            }
        }

        @Override
        public Object call() throws Exception {
            return new Object();
        }
    }
}
