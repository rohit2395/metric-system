package com.rohit;

import com.rohit.metrics.MetricsKey;
import com.rohit.metrics.SetGetMetrics;
import com.rohit.metrics.SetPutMetric;
import com.rohit.monitoring.MetricsManager;
import com.rohit.monitoring.MetricsOption;
import com.rohit.monitoring.MetricsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RecordMetrics {

    private final SetPutMetric setMetrics;
    private final SetGetMetrics getMetrics;

    private MetricsKey putKey;
    private MetricsKey getKey;
    /** Used in collecting stats */
    public static final List<MetricsOption> options = new ArrayList<MetricsOption>();

    public RecordMetrics(){
        putKey = new MetricsKey();
        getKey = new MetricsKey();
        options.add(MetricsOption.BYTES_UP);
        options.add(MetricsOption.BYTES_DOWN);
        options.add(MetricsOption.PUT_TIME);
        options.add(MetricsOption.GET_TIME);
        options.add(MetricsOption.TOTAL_SUCCESSFUL_PUTS);
        options.add(MetricsOption.TOTAL_SUCCESSFUL_GETS);

        /*
         * Add list of stats metrics for McStore RAS
         */
        System.out.println("Setting stats for metrics RAS");
        options.add(MetricsOption.GET_ERRORS_TIME);
        options.add(MetricsOption.PART_ERRORS_TIME);
        options.add(MetricsOption.PERSISTED_BYTES_DOWN);
        options.add(MetricsOption.PERSISTED_BYTES_UP);
        options.add(MetricsOption.PERSISTED_GET_TIME);
        options.add(MetricsOption.PERSISTED_PUT_TIME);
        options.add(MetricsOption.TOTAL_FAILED_GETS);
        options.add(MetricsOption.TOTAL_FAILED_PUTS);
        options.add(MetricsOption.TOTAL_GET_ERRORS);
        options.add(MetricsOption.TOTAL_GET_RETRIES);
        options.add(MetricsOption.TOTAL_PART_ERRORS);
        options.add(MetricsOption.TOTAL_PARTS_PUT);
        options.add(MetricsOption.TOTAL_PERSISTED_PARTS);
        options.add(MetricsOption.TOTAL_PUT_RETRIES);

        setMetrics = new SetPutMetric(null);
        getMetrics = new SetGetMetrics(null);
    }

    public void putBlob(int size) throws InterruptedException {
        System.out.println("Performing PUT Blob");
        setMetrics.isFinished(false);
        setMetrics.isEof(false);

        setMetrics.startPut(size);

        setMetrics.isWriting(true);
        TimeUnit.SECONDS.sleep(1);
        setMetrics.setSuccess();

        setMetrics.isWriting(false);
        setMetrics.isFinished(false);
        setMetrics.isEof(true);
        TimeUnit.SECONDS.sleep(1);
        setMetrics.setSuccess();

        System.out.println("PUT Blob done!");
    }
    public void getBlob(int size) throws InterruptedException {
        System.out.println("Performing GET Blob");
        getMetrics.startRead();
        TimeUnit.SECONDS.sleep(1);
        getMetrics.incBytesRead(size);
        getMetrics.setSuccess();
        System.out.println("GET Blob done!");
    }

    public void putBlobFailed(int size) throws InterruptedException {
        System.out.println("Performing PUT Blob FAILED");
        setMetrics.setFailure();
    }
    public void getBlobFailed(int size) throws InterruptedException {
        System.out.println("Performing GET Blob FAILED");
        getMetrics.setFailure();
    }

    public void putBlobRetry(int size) throws InterruptedException {
        System.out.println("Performing PUT Blob RETRY");
    }

    public void getBlobRetry(int size) throws InterruptedException {
        System.out.println("Performing GET Blob RETRY");
    }

    public void printStats(){
        try{
            System.out.println("Printing stats");
            MetricsResult putResult = MetricsManager.getMetricsManager().getMetrics(options,putKey);
            MetricsResult getResult = MetricsManager.getMetricsManager().getMetrics(options,getKey);
            System.out.println("Total bytes uploaded: "+putResult.getBytesUp());
            System.out.println("Total successful PUT operations: "+putResult.getTotalSuccessfulPuts());
            System.out.println("Total bytes downloaded: "+getResult.getBytesDown());
            System.out.println("Total successful GET operations: "+getResult.getTotalSuccessfulGets());
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
