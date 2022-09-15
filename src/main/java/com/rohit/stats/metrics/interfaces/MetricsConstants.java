package com.rohit.stats.metrics.interfaces;

public interface MetricsConstants {
    // Persistent "Totals" Counters
    // TotalBytesUp is the running sum of all the bytes we successfully transferred to the cloud
    // TotalPersistedBytesUp is the running sum of all the bytes we successfully transfered, that
    //    were part of a successful "PUT" operation.  The difference is that that we may have
    //    a 100 byte file, of which we successfully had 80 bytes sent to the cloud, but the last
    //    20 errored out, and the entire request failed.  So in that case we'd have :
    //    TotalBytesUp = 80
    //    TotalPersistedBytesUp = 0
    //
    // Likewise, TotalFailedRequests is how many requests completely failed (due to say
    //    Max # of retries being hit.  Where as, TotalNumberOfRetries, is how many times
    //    we retried a Get or a Put.. the overall operation may have been successful, but
    //    we had to retry a put 5 times to make it so.
    //
    // The difference in TotalFailedRequests and TotalErrors is that TotalGetErrors is
    //    the number of times a read from the cloud failed, but since we retry, the overall
    //    request may have been successful.

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
