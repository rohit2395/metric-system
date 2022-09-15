package com.rohit.stats;

import com.rohit.stats.metrics.MetricsKey;
import com.rohit.stats.metrics.AzureMetrics;
import com.rohit.stats.metrics.AwsMetrics;
import com.rohit.stats.metrics.OverallMetrics;
import com.rohit.stats.monitoring.MetricsManager;
import com.rohit.stats.monitoring.MetricsOption;
import com.rohit.stats.monitoring.MetricsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RecordMetrics {

    private AwsMetrics awsMetrics;
    private AzureMetrics azureMetrics;

    private MetricsKey awsKey;
    private MetricsKey azureKey;
    /** Used in collecting stats */
    public static final List<MetricsOption> options = new ArrayList<MetricsOption>();

    public RecordMetrics(){
        awsKey = AwsMetrics.DEFAULT_KEY;
        azureKey = AzureMetrics.DEFAULT_KEY;

        options.add(MetricsOption.BYTES_UP);
        options.add(MetricsOption.BYTES_DOWN);
        options.add(MetricsOption.PUT_TIME);
        options.add(MetricsOption.GET_TIME);
        options.add(MetricsOption.TOTAL_SUCCESSFUL_PUTS);
        options.add(MetricsOption.TOTAL_SUCCESSFUL_GETS);

        new Thread(() -> awsMetrics = new AwsMetrics(awsKey)).start();

        new Thread(() -> azureMetrics = new AzureMetrics(azureKey)).start();
    }

    public void putBlobAws(int size) throws InterruptedException {
        System.out.println("Performing PUT Blob");
        awsMetrics.incTotalBytesUp(size);
        awsMetrics.startPutTime();
        TimeUnit.SECONDS.sleep(2);
        awsMetrics.incTotalPutTime();
        awsMetrics.incTotalSuccessfulPutRequests();
        System.out.println("PUT Blob done!");
    }
    public void getBlobAws(int size) throws InterruptedException {
        System.out.println("Performing GET Blob");
        awsMetrics.incTotalBytesDown(size);
        awsMetrics.startGetTime();
        TimeUnit.SECONDS.sleep(4);
        awsMetrics.incTotalGetTime();
        awsMetrics.incTotalSuccessfulGetRequests();
        System.out.println("GET Blob done!");
    }

    public void putBlobAzure(int size) throws InterruptedException {
        System.out.println("Performing PUT Blob");
        azureMetrics.incTotalBytesUp(size);
        azureMetrics.startPutTime();
        TimeUnit.SECONDS.sleep(6);
        azureMetrics.incTotalPutTime();
        azureMetrics.incTotalSuccessfulPutRequests();
        System.out.println("PUT Blob done!");
    }
    public void getBlobAzure(int size) throws InterruptedException {
        System.out.println("Performing GET Blob");
        azureMetrics.incTotalBytesDown(size);
        azureMetrics.startGetTime();
        TimeUnit.SECONDS.sleep(6);
        azureMetrics.incTotalGetTime();
        azureMetrics.incTotalSuccessfulGetRequests();
        System.out.println("GET Blob done!");
    }

    public void printStatsAll(){
        try{
            System.out.println("==============================================");
            System.out.println("Overall Stats");
            System.out.println("==============================================");
            MetricsResult totalStats = MetricsManager.getMetricsManager().getMetrics(options, OverallMetrics.getOverallMetrics().getMetricsKey());
            System.out.println("Total bytes uploaded: "+totalStats.getBytesUp());
            System.out.println("Total bytes upload time: "+totalStats.getPutTime());
            System.out.println("Total successful PUT operations: "+totalStats.getTotalSuccessfulPuts());
            System.out.println("Total bytes downloaded: "+totalStats.getBytesDown());
            System.out.println("Total bytes download time: "+totalStats.getGetTime());
            System.out.println("Total successful GET operations: "+totalStats.getTotalSuccessfulGets());
            System.out.println("==============================================");

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void printStatsAws(){
        try{
            System.out.println("==============================================");
            System.out.println("AWS Stats");
            System.out.println("==============================================");
            MetricsResult awsStats = MetricsManager.getMetricsManager().getMetrics(options, awsKey);
            System.out.println("Total bytes uploaded: "+awsStats.getBytesUp());
            System.out.println("Total bytes upload time: "+awsStats.getPutTime());
            System.out.println("Total successful PUT operations: "+awsStats.getTotalSuccessfulPuts());
            System.out.println("Total bytes downloaded: "+awsStats.getBytesDown());
            System.out.println("Total bytes download time: "+awsStats.getGetTime());
            System.out.println("Total successful GET operations: "+awsStats.getTotalSuccessfulGets());
            System.out.println("==============================================");

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void printStatsAzure(){
        try{
            System.out.println("==============================================");
            System.out.println("AZURE Stats");
            System.out.println("==============================================");
            MetricsResult azureStats = MetricsManager.getMetricsManager().getMetrics(options, azureKey);
            System.out.println("Total bytes uploaded: "+azureStats.getBytesUp());
            System.out.println("Total bytes upload time: "+azureStats.getPutTime());
            System.out.println("Total successful PUT operations: "+azureStats.getTotalSuccessfulPuts());
            System.out.println("Total bytes downloaded: "+azureStats.getBytesDown());
            System.out.println("Total bytes download time: "+azureStats.getGetTime());
            System.out.println("Total successful GET operations: "+azureStats.getTotalSuccessfulGets());
            System.out.println("==============================================");

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
