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
public class AzureMetrics extends MetricsCounters implements MetricsConstants, MetricSet {

    private final HashMap<String, Metric> metricMap;

    private final MetricsKey metricsKey;
    public static final MetricsKey DEFAULT_KEY = new MetricsKey(AZURE_METRICS);

    //private static final Logger LOG = LoggerFactory.getLogger(JCloudGetMetricSet.class);

    /**
     * Initializes all the counters for this set of metrics and puts them into one nice little
     *    metric set package.
     */
    public AzureMetrics(MetricsKey key) {

//        System.out.println("Initializing Set Get metrics");
        // First, figure out our Keys so we can get the appropriate
        //   OverallMetrics instance, as well as generating appropriate
        //   keys for our metrics.
        if (Objects.nonNull(key)) {
            metricsKey = key;
        } else {
            metricsKey = DEFAULT_KEY;
        }
        overallMetrics = OverallMetrics.getOverallMetrics();
        metricMap = new HashMap<String,Metric>(6);
        metricMap.put( buildKey( TotalBytesUp ), bytesUp );
        metricMap.put( buildKey( TotalPutTime ), putTime );
        metricMap.put( buildKey( TotalSuccessfulPutRequests ), successfulPutRequests) ;
        metricMap.put( buildKey( TotalBytesDown ), bytesDown );
        metricMap.put( buildKey( TotalGetTime ), getTime );
        metricMap.put( buildKey( TotalSuccessfulGetRequests ), successfulGetRequests );

        //LOG.error("new GettMetric set root:id = {} : {}, counters left={}",root,requestId,OverallMetrics.getCount() );
//        System.out.println("new Get Metric set");
//        System.out.println("Key : "+this.metricsKey);
//        System.out.println("Request id: "+OverallMetrics.getCount());
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
        String key = MetricRegistry.name(this.metricsKey.getKeyName(),keyId ) ;
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


}
