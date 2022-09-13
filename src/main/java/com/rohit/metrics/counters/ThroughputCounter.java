package com.rohit.metrics.counters;

import com.codahale.metrics.Counter;

/**
 * class ThroughputCounter
 *
 * Implements a JMX counter interface to calculate and return the throughput of a csap.
 *
 * The set( long ) interface takes a number, and simply sets it.  It then returns
 *     that value on the getCount().  To reset, simply call with a value of 0.
 *
 *
 */

public class ThroughputCounter extends Counter {
    private volatile long time, bytes;

    public ThroughputCounter() {
        super();
        time = bytes = 0;
    }

    /**
     * called to add the data bytes transferred in this iteration and time taken.  The time is in milli seconds.
     *
     * @param bytes
     * @param time
     */
    public synchronized void addDataPoint(long bytes, long time) {
        this.bytes += bytes;
        this.time += time;
    }

    public synchronized void addTime(long time) {
        this.time += time;
    }

    /**
     * Returns the throughput as bytes/second.
     */
    @Override
    public long getCount() {
        double timeInSec = (double)time/1000;
        if(timeInSec == 0) timeInSec = 1;
        long throughput = (long)(bytes/timeInSec);
        return throughput;
    }

}
