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
 * class  JCloudPutMetricSet
 *
 * This is meant to track Metric data where from putBlobSlice() where we are
 *    writing data to the cloud.  This tracks the metrics at the JClouds
 *    interface boundary.
 *
 * This class tracks the transient metrics for data being sent via JClouds
 *    to the cloud.  This measures the last step of the data's journey!
 *
 * The way this class is meant to be used is as follows :
 *   - When a putBlobSlice is called, an instance of this class is created
 *     to track this request.  For each call to putBlobSlice, one instance of this
 *     class will be created.  putBlobSlice is what gets called when "putBlob()" is
 *     called on the AsyncBlobStore class.
 *   - The instance of this class gets associated with the BCSPayloadByteSource
 *     object, as well as the BCSPayloadInputStream object(s), (PIS for short)
 *   - At certain key points, the PIS object invokes methods here to let
 *     us know what's going on.
 *   - Using our incredible intellect we discern all sorts of things, update
 *     our transient metrics as well as the persisted ones in OverallMetrics.
 *   - PIS, however, can't tell us when the entire request has completed, or
 *     if it was successful or not.  For that, we need PutBlobSlice, in
 *     BlobStoreConnection to tell us.
 *   - To track our transient data, we extend MetricSet, and register ourselves
 *     with OverallMetrics (OM).  OM creates the persistent metrics, and provides
 *     us with methods to update those metrics.
 *   - By registering with OM, we allow customers to track individual requests
 *     and can see how each request is progressing.
 *
 *
 */

public class SetPutMetric extends MetricsConstants implements MetricSet {

    private final Counter bytesWritten = new Counter();  // every successful "put" incs this.
    private final Counter retries = new Counter();
    private final Counter errors = new Counter();
    private final Counter errorTime = new Counter();
    private final Counter timeSpent = new Counter();  // doesn't include retries
    private final Counter partsThisPut = new Counter();
    private final TimeCounter timeSpentThisSlice = new TimeCounter();
    private final HashMap<String,Metric> metricMap;

    private final MetricsKey metricsKey;
    private static final MetricsKey DEFAULT_KEY = new MetricsKey();
    private final String requestId;
    private final OverallMetrics overallMetrics;

    private volatile long bytesWriting,    // Number of bytes currently trying to write
            currentPosition, // It's like a file pointer
            thisPartBytesPut,//
            partBytesPut;    // each successful part of mpu incs this

    private volatile boolean hitEof,      // detected we reached the end of the file
            writing,     // it's complicated....
            retryExpected, // very complicated...
            finished;    // make sure we don't do it >1 time
    static private long nextRequestId;

    // parms to use on the putDone() call.
    private static boolean PUT_SUCCESS = true;
    private static boolean PUT_FAILED = false;
    //private static final Logger LOG = LoggerFactory.getLogger(JCloudPutMetricSet.class);


    /**
     * Initializes all the counters for this set of metrics and puts them into one nice little
     *    metric set package.  We take the # of bytes we expect to put on the constructor,
     *    but it turns out we don't really need it, or use it.. Keeping it here
     *    though.
     */
    public SetPutMetric( MetricsKey key) {
        System.out.println("Initializing SetPutMetric");

        // First, figure out our Keys so we can get the appropriate
        //   OverallMetrics instance, as well as generating appropriate
        //   keys for our metrics.
        // Always use a unique request id, even with MPO.  The rationale is
        //   it's just safer, and allows >1 backend threadset to be
        //    working on a file and the same time.
        if (Objects.nonNull(key)) {
            metricsKey = key;
            requestId = key.getCsap() + "-" + Long.toString( getNextRequestId() );
        } else {
            metricsKey = DEFAULT_KEY;
            requestId = Long.toString( getNextRequestId() );
        }
        overallMetrics = OverallMetrics.getMetrics( metricsKey );
        overallMetrics.incOngoingPuts();

        // --- Group the counters for this request in 1 MetricMap table -----
        metricMap = new HashMap<String,Metric>(7);
        metricMap.put( buildKey( PutJCloudBytesWritten ),bytesWritten);
        metricMap.put( buildKey( PutJCloudErrors ),errors);
        metricMap.put( buildKey( PutJCloudRetries ),retries);
        metricMap.put( buildKey( PutJCloudErrorTime ),errorTime);
        metricMap.put( buildKey( PutJCloudTimeSpent ),timeSpent );
        metricMap.put( buildKey( PutJCloudTimeSpentThisSlice ),timeSpentThisSlice );
        metricMap.put( buildKey( PutJCloudParts ),partsThisPut );

        // --- Register with the "manager" the metrics for the request being done  -----
        //LOG.error("new PutMetric set root : id = {} : {}, counters = {}",root,requestId,OverallMetrics.getCount() );
//        System.out.println("new Put Metric set");
//        System.out.println("Key : "+this.metricsKey);
//        System.out.println("Request id: "+OverallMetrics.getCount());
        OverallMetrics.registerMetricSet(this);
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
        String key = MetricRegistry.name( PutBase,this.metricsKey.getContainer(),this.metricsKey.getCsap(),requestId,keyId ) ;
        return key;
    }

    /**
     * startPut()
     *
     * Signals we copied data into jclouds buffer and are now waiting for
     *    some type of response to indicate that it was either successful,
     *    or failed.
     *
     * This is also where we log the number of times we "retry" put
     *    operations.  Keep in mind, these are retries for the entire "part",
     *    not just a teeny tiny slice.
     */
    public void startPut( int someNumber ) {
        //LOG.error("startPut() - retryExpected:{}, bytesWriting:{}",retryExpected,someNumber );
        writing = true;
        bytesWriting = someNumber;
        System.out.println("Bytes writing: "+bytesWriting);
        timeSpentThisSlice.setStartTime();
        if (retryExpected) {
            retryExpected = false;
            retries.inc();
            overallMetrics.incTotalNumberOfPutRetries();
        }
    }  // -- end of startPut() --

    /**
     * requestedMore()
     *
     * Let's the metric system know that JClouds has requested more bytes
     *   to get put up to the cloud.  This could happen in one of several
     *   conditions :
     *   1 - after an Open, first time good path
     *   2 - after a skip :
     *       2a - MPU, good path
     *       2b - a put failure, this follows the close/open/skip sequence
     *   3 - follows a successful write/put to the cloud
     *
     * If #1,  there is nothing to do here.
     *    #2a, nothing to do here
     *    #2b, we're doing a retry, skip inc's the counters.
     *    #3, this means the prior put has finished successfully.  update counters.
     */
    public void requestedMore() {
        //LOG.error("requestedMore() - writing:{}",writing );
        if ( writing ) putDone( PUT_SUCCESS );
    }

    /**
     * hitEof()
     *
     * Technically this entry getting called would signify a successful putBlobSlice()
     *    having completed.  However, due to the official "ending" not occurring
     *    until control is returned to BlobStoreConnection, we will only log a successful
     *    put.
     */
    public void hitEof() {
        //LOG.error("hitEof() - writing:{}",writing );
        if ( writing ) putDone( PUT_SUCCESS );
        hitEof = true;
    }  // -- end of hitEof() --

    /**
     * putDone()
     *
     * Called when we detect a put to Jclouds has finished.  Boolean
     *    parm indicates if the put was successful or not.
     *
     * @param success
     */
    private void putDone( boolean success ) {
        writing = false;
        long sliceTimeSpent = timeSpentThisSlice.getCount();
        if (success) {
            currentPosition += bytesWriting;
            thisPartBytesPut += bytesWriting;
            timeSpent.inc( sliceTimeSpent );
            bytesWritten.inc( bytesWriting );
            overallMetrics.incTotalBytesUp( bytesWriting );   // GOOD
            overallMetrics.incTotalPutTime( sliceTimeSpent ); // BAD
        }
        else {
            errors.inc();
            errorTime.inc( sliceTimeSpent );
            overallMetrics.incTotalPartErrors();
            overallMetrics.incTotalPartErrorsTime( sliceTimeSpent );
        }
        //LOG.error("putDone() - success:{}, timeThisSlice:{}, currentPos:{}, bytesWriting:{}",
        //          success,sliceTimeSpent,currentPosition,bytesWriting );
        bytesWriting = 0;
    }

    /**
     * partDone()
     *
     * Called when we detected a "part" was finished.  This could be :
     *   1 - eof was hit and the blob is done
     *   2 - skip was called to start a new MPU part
     */
    private void partDone() {
        //LOG.error("partDone() - thisPartBytesPut:{}, partBytesPut:{}",thisPartBytesPut,partBytesPut );
        partsThisPut.inc();
        overallMetrics.incTotalPartsPut();  // GOOD
        partBytesPut += thisPartBytesPut;
        thisPartBytesPut = 0;  // reset as we're starting a new part potentially
    }  // -- end of partDone() --
    /**
     * openedStream()
     *
     * Called when a new BCSPayloadInputStream is created.  This happens :
     *   1 - Start of a new Request
     *   2 - Start of a new MPU part
     *   3 - When an error occurs and we try to do a retry of the previous
     *       put.  After a close.  How to detect good vs bad?
     *
     * Again, until skip is called, we don't know what the bloody hell
     *    is going on cause we don't really control the code directing
     *    this opera.
     */
    public void openedStream() {
        //LOG.error("openedStream()" );
    }  // -- end of openedStream() --

    /**
     * closedStream()
     *
     * Called when JClouds closes the InputStream.  This could be :
     *   1 - after a successful write and things are "finished"
     *   2 - write failed and stream is being closed.
     *
     * We can't really detect which case this is, so we're going to
     *   have to punt this one, and try to solve it later.
     *
     * ?? Do we try to complicate matters further by recording the
     *    time we detected the "jcloud" op returned?  My opinion is
     *    not just no, but *** NO!!!
     */
    public void closedStream() {
        //LOG.error("closedStream()" );
    }  // -- end of closedStream() --

    /**
     * skipCalled()
     *
     * Called after an open when JClouds either :
     *   1 - Is starting a new MPU part
     *   2 - Is doing a retry from an error
     *
     * It is here that we need to determine whether the skip is #1 or #2 above.
     *   How to do that?
     *
     * Ok.  If #1 then the write was successful, then a close happened,
     *    then a skip to where that write would have left off.
     * If #2, then the skip will be to some "prior" portion of the stream,
     *    most likely to what we currently have as "currectPosition".
     *
     * @param skipTo
     */
    public void skipCalled( long skipTo ) {
        // if currentPosition + bytesWriting = someNumber, then
        //   we can assume we're good.. as it's picking up from where
        //   we expect a GOOD scenario to pick up from.  Only if we
        //   haven't yet processed the last "write"..
        //
        // Ale : would we ever get here w/o "writing" still being on??
        //LOG.error("skipCalled() - writing:{},currentPos:{},bytesWriting:{},skipTo:{}",
        //          writing,currentPosition,bytesWriting,skipTo );
        if (writing) {
            long goodPosition = currentPosition + bytesWriting;
            if (skipTo == goodPosition) {
                putDone( PUT_SUCCESS );
                partDone();
            }
            else {
                putDone( PUT_FAILED );
                currentPosition = skipTo;
                // not guaranteed jclouds will retry the put, so keep track
                //   that we expect it..
                retryExpected = true;
            }
        }
    }  // -- end of skipCalled() --

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
     * setSuccess()
     *
     * Called to notify the metrics system that this request has finished
     *   successfully.
     */
    public void setSuccess() {
        //LOG.error("PutMetric.setSuccess() root:id = {} : {}, counters left={}",root,requestId,OverallMetrics.getCount() );
        System.out.printf("PutMetric.setSuccess() key = %s : %s, counters left=%d\n",this.metricsKey,requestId,OverallMetrics.getCount() );
        //LOG.error("setSuccess() - finished:{}, writing:{}, hitEof:{}", finished,writing,hitEof );
        if ( finished ) return;
        if ( writing ) {
            System.out.println("Partial done");
            putDone( PUT_SUCCESS );
            partDone();
        }
        else if (hitEof) {
            System.out.println("Hit EOF");
            hitEof = false;
            partDone();
        }
        overallMetrics.incTotalPersistedBytesUp( partBytesPut );          // good
        overallMetrics.incTotalSuccessfulPutRequests();                   // good
        overallMetrics.incTotalPartsPersisted( partsThisPut.getCount() ); // good
        overallMetrics.incTotalPersistedPutTime( timeSpent.getCount() );  // <BAD> doesn't include retry time.. should it???
        // should there be another counter??
        overallMetrics.addCsapMigrateThroughputDataPoint(partBytesPut, timeSpent.getCount()); // TODO: is this too frequent?
        //LOG.error("PutMetric cleaning up root:id = {} : {}, counters left={}",root,requestId,OverallMetrics.getCount() );
        System.out.printf("PutMetric cleaning up key = %s : %s, counters left=%d\n",this.metricsKey,requestId,OverallMetrics.getCount() );
        overallMetrics.decOngoingPuts();
        OverallMetrics.cleanupMetricSet( this );
        finished = true;
    }  // -- end of setSuccess() --

    /**
     * setFailure()
     *
     * Called to notify the metrics system that this get request just failed.
     */
    public void setFailure() {
        //LOG.error("PutMetric.setFailure() root:id = {} : {}, counters left={}",root,requestId,OverallMetrics.getCount() );
        System.out.printf("PutMetric.setFailure() key = %s : %s, counters left=%d\n",this.metricsKey,requestId,OverallMetrics.getCount() );
        if ( finished ) return;
        if ( writing ) putDone( PUT_FAILED );
        overallMetrics.incTotalFailedPutRequests();
        overallMetrics.decOngoingPuts();
        OverallMetrics.cleanupMetricSet( this );
        //LOG.error("PutMetric cleaned up root:id = {} : {}, counters left={}",root,requestId,OverallMetrics.getCount() );
        System.out.printf("PutMetric cleaned up key = %s : %s, counters left=%d\n",this.metricsKey,requestId,OverallMetrics.getCount() );
        finished = true;
    }  // -- end of setFailure() --

    /*
     * getNextRequestId()
     *
     * This routine needs to be static, or else there needs to be a
     *   synchronized block inside the method. simpler to just make
     *   a synchronized static method.
     */
    private static synchronized long getNextRequestId() {
//        System.out.println("Get next request id, current id : "+nextRequestId);
        return nextRequestId++;
    }  // -- end of getNextRequestId() --


    // Remove this method, it's for testing purpose

    public void isRetry(boolean val) { retryExpected = val; }
    public void isWriting( boolean val) { writing = val; }
    public void isEof ( boolean val ){ hitEof = val; }
    public void isFinished(boolean val) { finished = val;}
}
