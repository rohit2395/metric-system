package com.rohit.stats.metrics.interfaces;

import com.codahale.metrics.Counter;
import com.rohit.stats.counters.TimeCounter;
import com.rohit.stats.metrics.OverallMetrics;

public abstract class MetricsCounters {

    protected OverallMetrics overallMetrics;

    // --- "total" Put Metrics ----
    protected final Counter bytesUp = new Counter();
    protected final TimeCounter putTime = new TimeCounter();
    protected final Counter successfulPutRequests = new Counter();

    // --- "total" Get Metrics ----
    protected final Counter bytesDown = new Counter();
    protected final TimeCounter getTime = new TimeCounter();
    protected final Counter successfulGetRequests = new Counter();



    //-------------------------------------------------------------------------
    //   "Put" metric updater methods.  On a "per root" basis
    //-------------------------------------------------------------------------
    public void incTotalBytesUp( long someNumber ) {
        bytesUp.inc( someNumber );
        overallMetrics.incTotalBytesUp(someNumber);
    }
    public void startPutTime() {
        putTime.setStartTime();
    }
    public void incTotalPutTime() {
        long time = putTime.recordCurrentTime();
        putTime.inc( time );
        overallMetrics.incTotalPutTime(time);
    }
    public void incTotalSuccessfulPutRequests() {
        successfulPutRequests.inc();
        overallMetrics.incTotalSuccessfulPutRequests();
    }
    //-------------------------------------------------------------------------
    //   "Get" metric updater methods, on a per root basis
    //-------------------------------------------------------------------------
    public void incTotalBytesDown( long someNumber ) {
        bytesDown.inc( someNumber );
        overallMetrics.incTotalBytesDown(someNumber);
    }
    public void startGetTime() {
        getTime.setStartTime();
    }
    public void incTotalGetTime() {
        long time = getTime.recordCurrentTime();
        getTime.inc( time );
        overallMetrics.incTotalGetTime(time);
    }
    public void incTotalSuccessfulGetRequests() {
        successfulGetRequests.inc();
        overallMetrics.incTotalSuccessfulGetRequests();
    }

}
