package hy.tmc.core;

import hy.tmc.core.Cache;
import hy.tmc.core.domain.Course;
import java.util.Date;
import java.util.HashMap;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

public class CacheTest {

    @Test
    public void timestampTest() throws InterruptedException {
        Date date = new Date();
        Thread.sleep(10);
        Cache.update(new HashMap<Integer, Course>());
        assertNotEquals(Cache.getLastUpdated().getTime(), date.getTime());
    }
}
