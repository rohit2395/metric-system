package com.rohit.stats.metrics.interfaces;

/**
 * class MetricsConstants
 *
 * This class holds the constants required for the metric system
 */
public interface MetricsConstants {

    //  Denotes the name of the metric
    String DEFAULT = "DEFAULT";
    String AWS_METRICS = "AWS_METRICS";
    String AZURE_METRICS = "AZURE_METRICS";

    //  ---- Overall/Totals for Get request metrics ----
    String TotalBytesDown = "total.bytes.down";
    String TotalSuccessfulGetRequests = "total.successful.get.requests";
    String TotalGetTime = "total.get.request.time";
    String TotalBytesUp = "total.bytes.up";
    String TotalSuccessfulPutRequests = "total.successful.put.requests";
    String TotalPutTime 	= "total.put.time";


}
