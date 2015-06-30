package hy.tmc.cli.synchronization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.junit.Test;
import org.junit.Before;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PollSchedulerTest {

    private PollScheduler pollScheduler;
    private AbstractScheduledService testService;
    private Thread schedulerThread;
    private long lastTime = 0;
    private long deltaTime = 0;
    private List<Long> times;

    @Before
    public void before() throws Exception {
        times = new ArrayList<>();
        this.pollScheduler = new PollScheduler(100, TimeUnit.MILLISECONDS);
        testService = new AbstractScheduledService() {
            @Override
            protected void runOneIteration() throws Exception {
                deltaTime = System.currentTimeMillis() - lastTime;
                times.add(deltaTime);
                lastTime = System.currentTimeMillis();
            }

            @Override
            protected Scheduler scheduler() {
                return pollScheduler;
            }
        };
        schedulerThread = new Thread() {
            @Override
            public void run() {
                try {
                    testService.start();
                } catch (Exception v) {
                    System.err.println(v.getMessage());
                }
            }

            @Override
            public void interrupt() {
                testService.stop();
                super.interrupt();
            }
        };
    }

    @Test
    public void schedulerShouldStayInTime() throws Exception {
        schedulerThread.run();
        Thread.sleep(1000);
        schedulerThread.interrupt();
        cleanFirstTime();
        long average = countAverage();
        assertEquals(pollScheduler.getInterval(), average);
    }

    @Test
    public void changeSchedulerShouldChangeSchedulerTime() throws Exception {
        schedulerThread.run();
        Thread.sleep(500);
        pollScheduler.changeSchedule(50, TimeUnit.MILLISECONDS);
        Thread.sleep(500);
        schedulerThread.interrupt();
        cleanFirstTime();
        HashSet<Long> longs = new HashSet<>(times);
        assertTrue(longs.contains(50L));
        assertTrue(longs.contains(100L));
    }

    private long countAverage() {
        long sum = 0;
        for (long delta : times) {
            sum += delta;
        }
        return sum / times.size();
    }

    private void cleanFirstTime() {
        times.remove(0);
    }

}
