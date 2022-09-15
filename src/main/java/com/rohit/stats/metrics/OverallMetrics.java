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
 * This class is the core for Metric collection.
 * It holds the metric Registry that is used via JMX to read the metrics.
 *
 * This class creates is meant for maintaining an overall metrics count for SVC
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


    private OverallMetrics() {

        registry = new MetricRegistry();
        final JmxReporter reporter = JmxReporter.forRegistry(registry).build();
        reporter.start();

        this.metricsKey = new MetricsKey(DEFAULT);
        metricMap = new HashMap<String,Metric>(6);


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
