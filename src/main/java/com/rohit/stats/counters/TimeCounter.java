package com.rohit.stats.counters;

import com.codahale.metrics.Counter;

/**
 *
 * A TimeCounter to mimic keeping track of time since the counter was created.
 *
 * Override the methods inc() and getCount() as we will maintain a variable currentRecordedTime
 * to calculate and hold the time taken for that operation
 */

public class TimeCounter extends Counter {
    private long start;
    private long currentRecordedTime;

    public TimeCounter()
    {
        super();
        start = System.currentTimeMillis();
    }
    /**
     * setStartTime()
     *
     * Sets/Resets the start time of this counter to NOW!!
     */
    public void setStartTime() { setStartTime( System.currentTimeMillis()); }

    /**
     * setStartTime()
     *
     * Allows user to set where they want the time to have originated.
     *    This is useful for when a request starts, but sometime later
     *    this counter is created.
     */
    public void setStartTime( long startTime ) {
        start = startTime; }

    public long recordCurrentTime()
    {
        long end = System.currentTimeMillis();
        long currentRecordedTime = end - start;
        currentRecordedTime = currentRecordedTime / 1000;
        System.out.println("Time taken to perform this operation: "+currentRecordedTime);
        return currentRecordedTime;
    }  // -- end of recordCurrentTime() --

    /**
     * @param time
     * Increment the currentRecordedTime variable
     */
    @Override
    public void inc(long time) {
        currentRecordedTime = currentRecordedTime + time;
    }

    /**
     * @return currentRecordedTime
     */
    @Override
    public long getCount()
    {
        return currentRecordedTime;
    }
}
