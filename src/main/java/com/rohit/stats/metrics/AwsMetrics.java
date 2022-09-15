package com.rohit.stats.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.rohit.stats.metrics.interfaces.MetricsCounters;
import com.rohit.stats.metrics.interfaces.MetricsConstants;

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

public class AwsMetrics extends MetricsCounters implements MetricsConstants, MetricSet {

    private final HashMap<String,Metric> metricMap;

    private final MetricsKey metricsKey;
    public static final MetricsKey DEFAULT_KEY = new MetricsKey(AWS_METRICS);


    /**
     * Initializes all the counters for this set of metrics and puts them into one nice little
     *    metric set package.  We take the # of bytes we expect to put on the constructor,
     *    but it turns out we don't really need it, or use it.. Keeping it here
     *    though.
     */
    public AwsMetrics(MetricsKey key) {
//        System.out.println("Initializing AwsMetrics");

        // First, figure out our Keys so we can get the appropriate
        //   OverallMetrics instance, as well as generating appropriate
        //   keys for our metrics.
        // Always use a unique request id, even with MPO.  The rationale is
        //   it's just safer, and allows >1 backend threadset to be
        //    working on a file and the same time.
        if (Objects.nonNull(key)) {
            metricsKey = key;
        } else {
            metricsKey = DEFAULT_KEY;
        }

        overallMetrics = OverallMetrics.getOverallMetrics();
        // --- Group the counters for this request in 1 MetricMap table -----
        metricMap = new HashMap<String,Metric>(6);
        metricMap.put( buildKey( TotalBytesUp ),bytesUp);
        metricMap.put( buildKey( TotalPutTime ),putTime);
        metricMap.put( buildKey( TotalSuccessfulPutRequests ),successfulPutRequests);
        metricMap.put( buildKey( TotalBytesDown ),bytesDown);
        metricMap.put( buildKey( TotalGetTime ),getTime );
        metricMap.put( buildKey( TotalSuccessfulGetRequests ),successfulGetRequests );

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
        return MetricRegistry.name( this.metricsKey.getKeyName(),keyId );
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

}
