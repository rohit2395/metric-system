package com.rohit.stats.counters;

import com.codahale.metrics.Counter;

/**
 * class TimeCounter
 *
 * Use our own counter to mimic keeping track of time since the counter
 *    was created.
 *
 * Basically all the Counter class methods won't really matter.. as
 *    we override what getCount() means.  It will always return how
 *    many milliseconds have elapsed since this counter was created.
 *
 * This is especially useful for allowing the UI to see what requests
 *    are currently being worked on, and how long they've been going
 *    on for as the reader will call the getCount() method on this
 *    class, as we'll return a value that is meaningful, eg. the
 *    amount of time this request has been active for.
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

    @Override
    public void inc(long n) {
        currentRecordedTime = currentRecordedTime + n;
    }

    @Override
    public long getCount()
    {
        return currentRecordedTime;
    }
}
