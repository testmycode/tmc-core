package hy.tmc.core.synchronization;

import com.google.common.util.concurrent.AbstractScheduledService.CustomScheduler;
import java.util.concurrent.TimeUnit;

public class PollScheduler extends CustomScheduler {

    private long interval;
    private TimeUnit timeunit;

    private Schedule schedule;

    /**
     * Timefixed scheduler.
     * @param interval to init the task
     * @param timeunit type of interval
     */
    public PollScheduler(long interval, TimeUnit timeunit) {
        this.interval = interval;
        this.timeunit = timeunit;
        this.schedule = new Schedule(interval, timeunit);
    }

    @Override
    protected Schedule getNextSchedule() throws Exception {
        return schedule;
    }

    /**
     * Changes the interval.
     * @param interval to init the task
     * @param timeunit type of interval
     */
    public void changeSchedule(long interval, TimeUnit timeunit) {
        this.schedule = new Schedule(interval, timeunit);
    }

    public long getInterval() {
        return interval;
    }

}
