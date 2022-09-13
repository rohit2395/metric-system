package com.rohit.metrics;


import com.codahale.metrics.*;
//import com.codahale.metrics.jmx.JmxReporter;
import com.rohit.metrics.counters.SingleValueCounter;
import com.rohit.metrics.counters.ThroughputCounter;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
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

public class OverallMetrics extends MetricsConstants implements MetricSet {

    private final MetricsKey metricsKey;
    private final HashMap<String,Metric> metricMap;

    // --- "total" JCloud Get Metrics ----
    private final Counter totalBytesDown = new Counter();
    private final Counter totalGetBlobDataTime = new Counter();
    private final Counter totalGetBlobTime = new Counter();
    private final Counter totalGetErrors = new Counter();
    private final Counter totalGetErrorsTime = new Counter();
    private final Counter totalNumberOfGetRetries = new Counter();
    private final Counter totalPersistedBytesDown = new Counter();
    private final Counter totalPersistedGetTime = new Counter();
    private final Counter totalFailedGetRequests = new Counter();
    private final Counter totalSuccessfulGetRequests = new Counter();

    private final Counter ongoingGetRequests = new Counter();

    private final SingleValueCounter maxGetBlobSliceTime = new SingleValueCounter();

    // --- "total" JCloud Put Metrics ----
    //
    //  "Parts" are the MPU or Blob objects, which are
    //      created as a result of 1 or more puts.
    private final Counter totalBytesUp = new Counter();
    private final Counter totalPutTime = new Counter();
    private final Counter totalPartErrors = new Counter();
    private final Counter totalPartErrorsTime = new Counter();
    private final Counter totalNumberOfPutRetries = new Counter();
    private final Counter totalPersistedBytesUp = new Counter();
    private final Counter totalPersistedPutTime = new Counter();
    private final Counter totalFailedPutRequests = new Counter();
    private final Counter totalSuccessfulPutRequests = new Counter();
    private final Counter totalPartsPersisted = new Counter();
    private final Counter totalPartsPut = new Counter();
    private final Counter ongoingPutRequests = new Counter();
    // --- "CSAP usage metrics"  not get/put related ----
    //
    //  Each time a csap is chosen to talk to the cloud, the csapUsed will be
    //      incremented.  The CurrentLatency is updated each time the csap
    //      updates its counters.
    private final Counter csapUsed = new Counter();
    private final SingleValueCounter csapCurrentSampledLatency = new SingleValueCounter();
    private final ThroughputCounter csapMigrateThroughput = new ThroughputCounter();
    private final ThroughputCounter csapRecallThroughput = new ThroughputCounter();

    // --- "CSAP latency metrics" ---
    // Below set of metrics track the latency involved (if any), in getting a jcloud
    // n/w thread, after an operation (migrate/recall) is submitted to BSCDoneHandler
    // from within BlobStoreConnection.
    private final Counter csapMigrateQueueLatency = new Counter();
    private final Counter csapRecallQueueLatency = new Counter();

    // ---  Statics --------
    private static ArrayList<OverallMetrics> rootList;
    private static MetricRegistry registry;

    static {
        rootList = new ArrayList<OverallMetrics>( 1 );
        registry = new MetricRegistry();
        final JmxReporter reporter = JmxReporter.forRegistry(registry).build();
        reporter.start();
    }

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
    private OverallMetrics( MetricsKey metricsKey ) {
        this.metricsKey = metricsKey;
        metricMap = new HashMap<String,Metric>(19);

        System.out.println("Metric key: "+buildKey( TotalBytesDown ));
        // ---- Put "Get" Metrics into the map -----
        metricMap.put( buildKey( TotalBytesDown ),totalBytesDown );
        metricMap.put( buildKey( TotalGetBlobDataTime ),totalGetBlobDataTime );
        metricMap.put( buildKey( TotalGetBlobTime ),totalGetBlobTime );
        metricMap.put( buildKey( TotalGetErrors ),totalGetErrors );
        metricMap.put( buildKey( TotalGetErrorsTime ),totalGetErrorsTime );
        metricMap.put( buildKey( TotalNumberOfGetRetries ),totalNumberOfGetRetries );
        metricMap.put( buildKey( TotalPersistedBytesDown ),totalPersistedBytesDown );
        metricMap.put( buildKey( TotalPersistedGetTime ),totalPersistedGetTime );
        metricMap.put( buildKey( TotalFailedGetRequests ),totalFailedGetRequests );
        metricMap.put( buildKey( TotalSuccessfulGetRequests ),totalSuccessfulGetRequests );
        metricMap.put( buildKey( OngoingGetRequests ),ongoingGetRequests );

        // ---- Put "Put" Metrics into the map -----
        metricMap.put( buildKey( TotalBytesUp ),totalBytesUp );
        metricMap.put( buildKey( TotalPutTime ),totalPutTime );
        metricMap.put( buildKey( TotalPartErrors ),totalPartErrors );
        metricMap.put( buildKey( TotalPartErrorsTime ),totalPartErrorsTime );
        metricMap.put( buildKey( TotalNumberOfPutRetries ),totalNumberOfPutRetries );
        metricMap.put( buildKey( TotalPersistedBytesUp ),totalPersistedBytesUp );
        metricMap.put( buildKey( TotalPersistedPutTime ),totalPersistedPutTime );
        metricMap.put( buildKey( TotalFailedPutRequests ),totalFailedPutRequests );
        metricMap.put( buildKey( TotalSuccessfulPutRequests ),totalSuccessfulPutRequests );
        metricMap.put( buildKey( TotalPartsPersisted ),totalPartsPersisted );
        metricMap.put( buildKey( TotalPartsPut ),totalPartsPut );
        metricMap.put( buildKey( OngoingPutRequests ),ongoingPutRequests );

        // ---- Put "CSAP" Metrics into the map -----
        metricMap.put( buildKey( CsapUsed ),csapUsed );
        metricMap.put( buildKey( CsapCurrentSampledLatency ),csapCurrentSampledLatency );
        metricMap.put( buildKey( CsapMigrateThroughput ),csapMigrateThroughput );
        metricMap.put( buildKey( CsapRecallThroughput ),csapRecallThroughput );
        metricMap.put( buildKey( CsapMigrateQueueLatency), csapMigrateQueueLatency);
        metricMap.put( buildKey( CsapRecallQueueLatency), csapRecallQueueLatency);


        OverallMetrics.registerMetricSet( this );
    }

    /**
     * getMetrics()
     *
     * Either finds an exiting metric set (OverallMetrics) for the given metrics key instance,
     *    or creates a new one if one doesn't exist.
     *
     */
    public static OverallMetrics getMetrics( MetricsKey metricsKey ) {
        System.out.println("Getting metric with key: "+metricsKey.getContainer());
        try {
            for( OverallMetrics om : rootList ) {
                System.out.println("Looping OverallMetrics");
                if (om.getMetricsKey().compareTo(metricsKey)==0){
                    System.out.println("Found the key");
                    return om;
                }
            }
        }
        catch( Exception e ) {}  // most likely a ConcurrentModificationException
        /* We get here either because the above code got an exception, most likely because
         *     the code below added an entry to the list, or because we didn't find an
         *     entry for metrics key instance.
         *  This is where we take our performance penalty, and we don't expect this path
         *     to get run but a few times per life of the JVM.  We Sync here, and then
         *     check the list again, in case another thread added the entry we want.
         *  We can't get the ConcurrentModificationException once we've synchronized.
         */
        synchronized( rootList ) {
            for( OverallMetrics om : rootList ) {
                System.out.println("Looping OverallMetrics");
                if (om.getMetricsKey().compareTo(metricsKey)==0) {
                    System.out.println("Found the key");
                    return om;
                }
            }
//            System.out.println("Key not found so initilizing OverallMetrics with this key");
            OverallMetrics om = new OverallMetrics( metricsKey );
            //LOG.error( "Core: adding key : {} to rootList.",root );
            System.out.printf("Core: adding key : %s,%s to rootList.\n",metricsKey.getContainer(),metricsKey.getCsap() );
            rootList.add( om );
            return om;
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
        String key = MetricRegistry.name( DEFAULT,this.metricsKey.getContainer(),this.metricsKey.getCsap(),keyId ) ;
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

            System.out.println("Registering "+ metrics.getMetrics().size() +" metric keys");
            for(Map.Entry<String, Metric> entry : metrics.getMetrics().entrySet()){
//                System.out.println("Registering metric: "+entry.getKey());
//                System.out.println("Value: "+((Counter)entry.getValue()).getCount());
                registry.register(entry.getKey(),entry.getValue());
            }
//            registry.registerAll( metrics );
        }
        // Don't fail the request due to this.
        catch( Exception e ) {
            System.out.printf("Core registerMetricSet Exception : %s\n",e.toString() );
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
    //   "Get" metric updater methods, on a per root basis
    //-------------------------------------------------------------------------
    public void incTotalBytesDown( long someNumber ) { totalBytesDown.inc( someNumber ); }
    public void incTotalGetBlobDataTime( long someNumber ) {
        totalGetBlobDataTime.inc( someNumber );
        if(someNumber > maxGetBlobSliceTime.getCount()) maxGetBlobSliceTime.set(someNumber);
    }
    public void incTotalGetBlobTime( long someNumber ) { totalGetBlobTime.inc( someNumber ); }
    public void incTotalGetErrors() { totalGetErrors.inc(); }
    public void incTotalGetErrorsTime( long someNumber ) { totalGetErrorsTime.inc( someNumber ); }
    public void incTotalNumberOfGetRetries() { totalNumberOfGetRetries.inc(); }
    public void incTotalPersistedBytesDown( long someNumber ) { totalPersistedBytesDown.inc( someNumber ); }
    public void incTotalPersistedGetTime( long someNumber ) { totalPersistedGetTime.inc( someNumber ); }
    public void incTotalFailedGetRequests() { totalFailedGetRequests.inc(); }
    public void incTotalSuccessfulGetRequests() { totalSuccessfulGetRequests.inc(); }
    public void incOngoingGets() { ongoingGetRequests.inc(); }
    public void decOngoingGets() { ongoingGetRequests.dec(); }

    //-------------------------------------------------------------------------
    //   "Put" metric updater methods.  On a "per root" basis
    //-------------------------------------------------------------------------
    public void incTotalBytesUp( long someNumber ) { totalBytesUp.inc( someNumber ); }
    public void incTotalPutTime( long someNumber ) { totalPutTime.inc( someNumber ); }
    public void incTotalPartErrors() { totalPartErrors.inc(); }
    public void incTotalPartErrorsTime( long someNumber ) { totalPartErrorsTime.inc( someNumber ); }
    public void incTotalNumberOfPutRetries() { totalNumberOfPutRetries.inc(); }
    public void incTotalPersistedBytesUp( long someNumber ) { totalPersistedBytesUp.inc( someNumber ); }
    public void incTotalPersistedPutTime( long someNumber ) { totalPersistedPutTime.inc( someNumber ); }
    public void incTotalFailedPutRequests() { totalFailedPutRequests.inc(); }
    public void incTotalSuccessfulPutRequests() { totalSuccessfulPutRequests.inc(); }
    public void incTotalPartsPersisted( long someNumber ) { totalPartsPersisted.inc( someNumber ); }
    public void incTotalPartsPut() { totalPartsPut.inc(); }
    public void incOngoingPuts() { ongoingPutRequests.inc(); }
    public void decOngoingPuts() { ongoingPutRequests.dec(); }


    //-------------------------------------------------------------------------
    //   "CSAP" metric updater methods.  On a "per key" basis : key=container/csap
    //-------------------------------------------------------------------------
    public void incCsapUsed() { csapUsed.inc(); }
    public void setCsapCurrentSampledLatency( long someNumber ) { csapCurrentSampledLatency.set( someNumber ); }
    public void addCsapMigrateThroughputDataPoint(long bytes, long time ) { csapMigrateThroughput.addDataPoint(bytes, time); }
    public void addCsapRecallThroughputDataPoint(long bytes, long time ) { csapRecallThroughput.addDataPoint(bytes, time); }
    public void addTimeToCsapRecallThroughputMetric(long time ) { csapRecallThroughput.addTime(time); }
    public void incCsapMigrateQueueLatency(long time) { csapMigrateQueueLatency.inc(time); }
    public void incCsapRecallQueueLatency(long time) { csapRecallQueueLatency.inc(time); }

    /**
     * Resets all metrics back to 0.
     *
     */
    public void resetMetrics() {
        synchronized(rootList) {
            cleanupMetricSet(this);
            rootList.remove(this);
        }
    }
}
