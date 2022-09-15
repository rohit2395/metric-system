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
 * class  AzureMetrics
 *
 * This class creates metrics to be used for Azuire CSP.
 *
 */
public class AzureMetrics extends MetricsCounters implements MetricsConstants, MetricSet {

    private final HashMap<String, Metric> metricMap;

    private final MetricsKey metricsKey;
    public static final MetricsKey DEFAULT_KEY = new MetricsKey(AZURE_METRICS);

    /**
     * Initializes the metric key for Aws Metrics.
     * Builds the metricMap by generating keys for every metric to be tracked
     * Registers the metrics in the platform metric registry
     */
    public AzureMetrics(MetricsKey key) {

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

        OverallMetrics.registerMetricSet( this );
    }

    /**
     * @param keyId
     * @return generated key
     */
    private String buildKey( String keyId ) {
        String key = MetricRegistry.name(this.metricsKey.getKeyName(),keyId ) ;
        return key;
    }


    @Override
    /**
     * @return metricMap
     */
    public Map<String, Metric> getMetrics() {
        return metricMap;
    }


}
