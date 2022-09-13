package com.rohit.metrics;

public class MetricsConstants {
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
    public final static String Base = "MCStore";
    public final static String DEFAULT = "DEFAULT",
            GetBase = "GetFromJcloud",
            PutBase = "PutToJcloud";

    //  ---- Overall/Totals for Get request metrics ----
    public final static String TotalBytesDown = "bytes.total.down";
    public final static String TotalPersistedBytesDown = "persisted.bytes.total.down";
    public final static String TotalSuccessfulGetRequests = "total.successful.get.requests";
    public final static String TotalFailedGetRequests = "failed.get.requests";
    public final static String TotalNumberOfGetRetries = "total.number.get.retries";
    public final static String TotalGetBlobDataTime = "total.get.data.time";
    public final static String TotalGetBlobTime = "total.get.request.time";
    public final static String TotalPersistedGetTime = "total.persisted.get.time";
    public final static String TotalGetErrorsTime = "total.get.errors.time";
    public final static String TotalGetErrors = "total.get.errors";
    public final static String OngoingGetRequests = "ongoing.get.requests";

    public final static String TotalBytesUp = "bytes.total.up";
    public final static String TotalPersistedBytesUp = "persisted.bytes.total.up";
    public final static String TotalSuccessfulPutRequests = "total.successful.put.requests";
    public final static String TotalFailedPutRequests = "failed.put.requests";
    public final static String TotalNumberOfPutRetries = "total.number.put.retries";
    public final static String TotalPutTime 	= "total.put.time";
    public final static String TotalPersistedPutTime = "total.persisted.put.time";
    public final static String TotalPartErrorsTime = "total.part.errors.time";
    public final static String TotalPartErrors = "total.part.errors";
    public final static String TotalPartsPersisted = "total.parts.persisted";
    public final static String TotalPartsPut = "total.parts.put";
    public final static String OngoingPutRequests = "ongoing.put.requests";

    // Counters for each "get" request from JClouds
    public final static String GetJCloudBytesRead = "JCloud.get.bytes.read";
    public final static String GetJCloudErrors = "JCloud.get.errors";
    public final static String GetJCloudRetries = "JCloud.get.retries";
    public final static String GetJCloudTimeSpent = "JCloud.get.time.spent";
    public final static String GetJCloudErrorTime = "JCloud.get.error.time";
    public final static String GetJCloudTimeSpentThisSlice = "JCloud.get.time.spent.this.slice";

    // Counters for each "put" request from JClouds
    public final static String PutJCloudBytesWritten = "JCloud.put.bytes.written";
    public final static String PutJCloudErrors = "JCloud.put.errors";
    public final static String PutJCloudRetries = "JCloud.put.retries";
    public final static String PutJCloudTimeSpent = "JCloud.put.time.spent";
    public final static String PutJCloudErrorTime = "JCloud.put.error.time";
    public final static String PutJCloudTimeSpentThisSlice = "JCloud.put.time.spent.this.slice";
    public final static String PutJCloudParts = "JCloud.put.parts";

    // CSAP specific counters for tracking CSAP usage, we only do 2 atm,
    //    1 - just tracks how many times this csap was chosen to talk to the cloud
    //    2 - tracks the latest stored latency time of the check we use to validate the csap
    public final static String CsapUsed = "csap.used";
    public final static String CsapCurrentSampledLatency = "csap.current.sampled.latency";
    public final static String CsapMigrateThroughput = "csap.migrate.throughput";
    public final static String CsapRecallThroughput = "csap.recall.throughput";
    public final static String CsapMigrateQueueLatency = "csap.migrate.queuelatency";
    public final static String CsapRecallQueueLatency = "csap.recall.queuelatency";
}
