package com.rohit.stats.metrics;


import com.codahale.metrics.*;
import com.rohit.stats.metrics.interfaces.MetricsCounters;
import com.rohit.stats.metrics.interfaces.MetricsConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * class - OverallMetrics
 *
 * This class is the core for Metric collection in MCStore.  It holds
 *    the metric Registry that is used via JMX to read the metrics.
 *
 * This class creates all of the persistent metrics, and provides interfaces/methods
 *    to update them.
 *
 * This class also provides the interface to register transient metric data, such
 *    as for requests that are currently being processed.
 *
 * The way ICStore metrics works is as follows.
 *
 *    - There are persistent metrics, created and tracked by this Class.
 *      This class, OverallMetrics, is a STATIC class and provides static
 *      methods for users (JcloudPut/GetMetricSet for example) to update
 *      the metrics we persist and show to the user.
 *
 *    - To update the persistent metrics, another class must be created
 *      that knows how to gather and update said metrics.  If that class
 *      wants it's own transient data shown, it needs to create a MetricSet
 *      and register it with this class, using the registerMetricSet() method.
 *
 *    - cleanupMetricSet() must also be called if one was registered, otherwise,
 *      the transient metric objects will not be cleaned up, creating a memory leak.
 *
 * So, if someone wants to add JuJu metrics they'd add the corresponding "persistedJuJu"
 *    metrics to this class, along with corresponding methods to update those metrics.
 *    In some other class, "JuJuMetrics" they'd have the logic for how to gather those
 *    metrics and update the persisted data here, using the static methods.  If they
 *    also wanted to have transient data, they'd create a MetricSet object, and
 *    register it here (and clean it up when done).
 *
 * The reason for making this just a static class, is that there would never be more
 *    than 1 instance of this class (a true singleton), and making everything static
 *    eliminates the need to try to obtain the object instance, and then the timing
 *    of when it gets created.  Here, everything is instantiated and created when
 *    we get referenced for the first time.  The static initializer makes sure
 *    everything is built and ready for use.
 *
 */

public class OverallMetrics extends MetricsCounters implements MetricsConstants, MetricSet {

    private final MetricsKey metricsKey;
    private final HashMap<String,Metric> metricMap;

    // ---  Statics --------
    private static OverallMetrics rootMetric = new OverallMetrics();
    private static MetricRegistry registry;

    public static int getCount() {
        return registry.getCounters().size();
    }

    @Override
    public Map<String, Metric> getMetrics() {
        return metricMap;
    }

    /*
     * OverallMetrics
     *
     * There's one of these per an instance of metrics key, or "default" metrics key instance that
     *    folks like SVC will use
     */
    private OverallMetrics() {

        registry = new MetricRegistry();
        final JmxReporter reporter = JmxReporter.forRegistry(registry).build();
        reporter.start();

        this.metricsKey = new MetricsKey(DEFAULT);
        metricMap = new HashMap<String,Metric>(6);

//        System.out.println("Metric key: "+buildKey( TotalBytesDown ));

        // ---- Put "Put" Metrics into the map -----
        metricMap.put( buildKey( TotalBytesUp ),bytesUp );
        metricMap.put( buildKey( TotalPutTime ),putTime );
        metricMap.put( buildKey( TotalSuccessfulPutRequests ),successfulPutRequests );

        // ---- Put "Get" Metrics into the map -----
        metricMap.put( buildKey( TotalBytesDown ),bytesDown);
        metricMap.put( buildKey( TotalGetTime ),getTime );
        metricMap.put( buildKey( TotalSuccessfulGetRequests ),successfulGetRequests );


        OverallMetrics.registerMetricSet( this );
    }

    /**
     * getMetrics()
     *
     * Either finds an exiting metric set (OverallMetrics) for the given metrics key instance,
     *    or creates a new one if one doesn't exist.
     *
     */
    public static OverallMetrics getOverallMetrics() {
//        System.out.println("Getting overall metrics");
        synchronized (rootMetric){
            return rootMetric;
        }
    }  // -- end of getMetrics() --

    public MetricsKey getMetricsKey() { return metricsKey; }

    /*
     * buildKey()
     *
     * Helper routine, understands how we build/structure our keys for the
     *    MetricMap.  Takes in a KeyId, and then assembles it with our
     *    Base metric name (MCStore), and the container & csap, which for some callers
     *    like SVC, will be "DEFAULT", and others, like GPFS, will be the
     *    container & csap combination
     *
     *  Key = "MCStore.<container>.<csap>.metric-name"
     */
    private String buildKey( String keyId ) {
        String key = MetricRegistry.name(this.metricsKey.getKeyName(),keyId ) ;
        return key;
    }


    /**
     * registerMetricSet()
     *
     * adds the given metrics in the set to the registry where they can
     *    be read/retrieved using JMX.  Callers must also be sure to
     *    cleanup by calling "cleanupMetricSet", otherwise, the metrics
     *    will exist in the registry for the life of the jvm.
     *
     * This is called by 2 different sets of users :
     *   1 - this class.. implementing the base metrics for a file system, or
     *       "root" key.
     *   2 - Request specific metrics, like JCloudPutMetricSet which keeps
     *       metrics around only as long as that request is running.  Once
     *       that request is done, it calls the cleanupMetricSet() routine
     *       to remove those "transient" metrics.
     *
     * @param metrics
     */
    public static void registerMetricSet( MetricSet metrics) {
        //LOG.error( "Core: registering MetricSet : {}.",metrics );
        try {

//            System.out.println("Registering "+ metrics.getMetrics().size() +" metric keys");
            for(String key : metrics.getMetrics().keySet()){
//                System.out.println("Registering Metric : "+key);
                registry.register(key,metrics.getMetrics().get(key));
            }
//            registry.registerAll( metrics );
        }
        // Don't fail the request due to this.
        catch( Exception e ) {
            System.out.printf("Core registerMetricSet Exception : %s\n",e.toString() );
            e.printStackTrace();
        }
    }

    /**
     * cleanupMetricSet()
     *
     * Removes the metrics from the registry, and also removes
     *   the metrics from the map.
     *
     * @param metrics
     */
    public static void cleanupMetricSet( MetricSet metrics ) {
        //LOG.error( "Core: cleanup MetricSet : {}.",metrics );
        Map<String,Metric> map = metrics.getMetrics();
        Set<String> keys = map.keySet();
        for ( String key : keys)
        {
            registry.remove( key );
        }
        map.clear();
    } // -- end of cleanupMetricSet() --

    //-------------------------------------------------------------------------
    //   "Put" metric updater methods.  On a "per root" basis
    //-------------------------------------------------------------------------
    public void incTotalBytesUp( long someNumber ) { bytesUp.inc( someNumber ); }
    public void incTotalPutTime( long someNumber ) { putTime.inc( someNumber ); }
    public void incTotalSuccessfulPutRequests() { successfulPutRequests.inc(); }
    //-------------------------------------------------------------------------
    //   "Get" metric updater methods, on a per root basis
    //-------------------------------------------------------------------------
    public void incTotalBytesDown( long someNumber ) { bytesDown.inc( someNumber ); }
    public void incTotalGetTime(long someNumber ) {
        getTime.inc( someNumber );
    }
    public void incTotalSuccessfulGetRequests() { successfulGetRequests.inc(); }

    /**
     * Resets all metrics back to 0.
     *
     */
    public void resetMetrics() {
        synchronized(rootMetric) {
            cleanupMetricSet(this);
            rootMetric = null;
        }
    }
}
