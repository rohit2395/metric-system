package com.rohit.metrics;


import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.rohit.metrics.counters.TimeCounter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *   JCloudGetMetricSet
 *
 *   This is used to generate Metric data for the GetBlobSlice operation where we
 *       read data from JClouds.  getBlobSlice() will instantiate an
 *       instance of this class, and then call the startRead(), and
 *       other routines to log data as it's being read from JClouds.
 *
 *   Once we get called from getBlobSlice() we update our own transient
 *       metrics, which track what this specific "getBlob()" request is
 *       doing.  We also update the persistent, OverallMetrics, related
 *       to reading data from JClouds.
 *
 *   OverallMetrics allows us to register a MetricSet (which we extend)
 *       to track the transient metrics specifically for this request.
 *       It allows consumers/users to be able to see what is happening
 *       with each get request currently being run.
 *
 *   We track :
 *
 *   bytesRead - total number of bytes we successfully read from JClouds.
 *               I don't think we can track how many we "failed" to read.
 *
 *   errors    - number of times we failed to read from the cloud
 *
 *   retries   - number of times we had to issue a retry on a read
 *
 *   errorTime - amount of time spent on failed/retry read requests
 *
 *   totalTimeSpent - tracks the total amount of time we wait to read data
 *                    from jclouds.  This is aggregated over how ever many
 *                    calls it takes to read all the data for this request.
 *                    This does NOT include time spent for retries.
 *
 *   timeSpentThisSlice - Just how long the current read request has taken.
 *                        This value is readable by the UI/console and thus
 *                        users could see we've waited "xxx" seconds on just
 *                        this current attempt.
 *
 *   Note : The time values are only how long it takes for JClouds to respond,
 *          it does not track the amount of time other modules may spend
 *          processing the data once we get it.
 *
 * @author Harold
 *
 */
public class SetGetMetrics extends MetricsConstants implements MetricSet {

    private final Counter bytesRead = new Counter();
    private final Counter retries = new Counter();
    private final Counter errors = new Counter();
    private final Counter errorTime = new Counter();
    private final Counter totalTimeSpent = new Counter();  // doesn't include retries
    private final TimeCounter timeSpentThisSlice = new TimeCounter();

    private final HashMap<String, Metric> metricMap;
    private final OverallMetrics overallMetrics;

    private final MetricsKey metricsKey;
    private static final MetricsKey DEFAULT_KEY = new MetricsKey();

    private final String requestId;

    private boolean readError,   // last read attempt timed out or otherwise failed,
            reading,     // starting to read.. this is a key for failure processing.
            finished;    // make sure we don't do it >1 time

    static private long nextRequestId;
    //private static final Logger LOG = LoggerFactory.getLogger(JCloudGetMetricSet.class);

    /**
     * Initializes all the counters for this set of metrics and puts them into one nice little
     *    metric set package.
     */
    public SetGetMetrics( MetricsKey key) {

        System.out.println("Initializing Set Get metrics");
        // First, figure out our Keys so we can get the appropriate
        //   OverallMetrics instance, as well as generating appropriate
        //   keys for our metrics.
        if (Objects.nonNull(key)) {
            metricsKey = key;
            requestId = key.getCsap() + "-" + Long.toString( getNextRequestId() );
        } else {
            metricsKey = DEFAULT_KEY;
            requestId = Long.toString( getNextRequestId() );
        }
        overallMetrics = OverallMetrics.getMetrics( metricsKey );

        overallMetrics.incOngoingGets();

        metricMap = new HashMap<String,Metric>(4);
        metricMap.put( buildKey( GetJCloudBytesRead ),bytesRead );
        metricMap.put( buildKey( GetJCloudErrors ),errors );
        metricMap.put( buildKey( GetJCloudRetries ),retries) ;
        metricMap.put( buildKey( GetJCloudErrorTime ),errorTime );
        metricMap.put( buildKey( GetJCloudTimeSpent ),totalTimeSpent );
        metricMap.put( buildKey( GetJCloudTimeSpentThisSlice ),timeSpentThisSlice );

        //LOG.error("new GettMetric set root:id = {} : {}, counters left={}",root,requestId,OverallMetrics.getCount() );
        System.out.println("new Get Metric set");
        System.out.println("Key : "+this.metricsKey);
        System.out.println("Request id: "+OverallMetrics.getCount());
        OverallMetrics.registerMetricSet( this );
    }

    /*
     * buildKey()
     *
     * Helper routine, understands how we build/structure our keys for the
     *    MetricMap.  Takes in a KeyId, and then assembles it with our
     *    Base metric name (MCStore), and the root, which for some callers
     *    like SVC, will be "DEFAULT", and others, like GPFS, will be the
     *    root of the filesystem (/xxx/yyy)
     *
     *  Key = "MCStore.<root>.metric-name"
     */
    private String buildKey( String keyId ) {
        String key = MetricRegistry.name( GetBase,this.metricsKey.getContainer(),this.metricsKey.getCsap(),requestId,keyId ) ;
        return key;
    }

    @Override
    /**
     * getMetrics()
     *
     * There should be no race conditions with our map, so handing back
     *    the actual map shouldn't be a problem, otherwise, we'd clone() it.
     */
    public Map<String, Metric> getMetrics() {
        return metricMap;
    }

    /**
     * startRead()
     *
     * If the prior read failed, the "readError" flag will be on.  That means
     *    this read is a "retry" attempt, so we'll increment the number of
     *    retries here and reset the flag.
     */
    public void startRead() {
        if (readError) {
            readError = false;
            retries.inc();
            overallMetrics.incTotalNumberOfGetRetries();
        }
        reading = true;
        timeSpentThisSlice.setStartTime();
    }

    public void incBytesRead( long numBytes ) {
        long thisSliceTime = timeSpentThisSlice.getCount();
        reading = false;
        totalTimeSpent.inc( thisSliceTime );
        bytesRead.inc( numBytes );
        overallMetrics.incTotalBytesDown( numBytes );
        overallMetrics.incTotalGetBlobDataTime( thisSliceTime );
    }

    public void incErrors() {
        long thisSliceTime = timeSpentThisSlice.getCount();
        if (reading) readError = true;  // this will let us count retries.
        reading = false;
        errorTime.inc( thisSliceTime );
        errors.inc();
        overallMetrics.incTotalGetErrors();
        overallMetrics.incTotalGetErrorsTime( thisSliceTime );
    }

    /**
     * setSuccess()
     *
     * Called to notify the metrics system that this get request succeeded
     *   and there are no more reads being done.  Not that the current slice
     *   time will not be added.
     * ??? should it ???
     */
    public void setSuccess() {
        //LOG.error("GetMetric.setSuccess() root:id = {} : {}",root,requestId );
        System.out.printf("GetMetric.setSuccess() key = %s : %s\n",this.metricsKey,requestId );
        if (!finished) {
            finished = true;
            overallMetrics.incTotalSuccessfulGetRequests();
            overallMetrics.incTotalPersistedBytesDown( bytesRead.getCount() );
            overallMetrics.incTotalPersistedGetTime( totalTimeSpent.getCount() );
            overallMetrics.addCsapRecallThroughputDataPoint(bytesRead.getCount(), totalTimeSpent.getCount());
            overallMetrics.decOngoingGets();
            OverallMetrics.cleanupMetricSet( this );
            //LOG.error("GettMetric cleaned up root:id = {} : {}, counters left={}",root,requestId,OverallMetrics.getCount() );
            System.out.printf("GettMetric cleaned up key = %s : %s, counters left=%d\n",this.metricsKey,requestId,OverallMetrics.getCount() );
        }
    }

    /**
     * setFailure()
     *
     * Called to notify the metrics system that this get request just failed.
     *
     * If we were attempting to read at the time, call the incErrors() routine
     *    to process the error information.
     */
    public void setFailure() {
        //LOG.error("GetMetric.setFailure() root:id = {} : {}",root,requestId );
        System.out.printf("GetMetric.setFailure() key = %s : %s\n",this.metricsKey,requestId );
        if (reading) {
            incErrors();
        }
        if (!finished) {
            finished = true;
            overallMetrics.incTotalFailedGetRequests();
            overallMetrics.decOngoingGets();
            OverallMetrics.cleanupMetricSet( this );
            //LOG.error("GettMetric cleaned up root:id = {} : {}, counters left={}",root,requestId,OverallMetrics.getCount() );
            System.out.printf("GettMetric cleaned up key = %s : %s, counters left=%d\n",this.metricsKey,requestId,OverallMetrics.getCount() );
        }
    }  // -- end of setFailure() --

    /*
     * getNextRequestId()
     *
     * This routine needs to be static, or else there needs to be a
     *   synchronized block inside the method. simpler to just make
     *   a synchronized static method.
     */
    private static synchronized long getNextRequestId() {
        return nextRequestId++;
    }  // -- end of getNextRequestId() --
}
