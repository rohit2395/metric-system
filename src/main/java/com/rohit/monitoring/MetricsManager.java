/*******************************************************************************
 * Licensed Materials - Property of IBM
 *  
 *  OCO Source Materials
 *  
 *  (C) Copyright IBM Corp. 2016-2017 All Rights Reserved
 *  
 *  The source code for this program is not published or other-
 *  wise divested of its trade secrets, irrespective of what has
 *  been deposited with the U.S. Copyright Office.
 *******************************************************************************/
package com.rohit.monitoring;

import com.rohit.metrics.MetricsConstants;
import com.rohit.metrics.MetricsKey;
import com.rohit.metrics.OverallMetrics;
import com.rohit.metrics.SetGetMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.List;

/**
 * This class handles the metrics management
 * 
 *
 */
public class MetricsManager
{

    private final static String METRICS_PREFIX = "metrics:name=";

    /**
     * Singleton instance of the class
     */
    private static MetricsManager metricsManager = new MetricsManager();

    protected MBeanServerConnection server;

    /**
     * Instantiate the server only once.
     */
    protected MetricsManager()
    {
        this.server = ManagementFactory.getPlatformMBeanServer();
    }

    static public MetricsManager getMetricsManager()
    {
        return metricsManager;
    }

    /**
     * Returns the metrics those kind of counting in Long format
     * 
     * @param name
     * @return
     * @throws MalformedObjectNameException
     * @throws AttributeNotFoundException
     * @throws InstanceNotFoundException
     * @throws MBeanException
     * @throws ReflectionException
     * @throws IOException
     */
    public Long getCountMetrics(String name, MetricsKey metricsKey)
                    throws MalformedObjectNameException, AttributeNotFoundException, InstanceNotFoundException,
                    MBeanException, ReflectionException, IOException
    {

        ObjectName metric = new ObjectName(METRICS_PREFIX + MetricsConstants.DEFAULT + "." + metricsKey.getContainer() + "." + metricsKey.getCsap() + "." + name);
        Long result=0L;
        try
        {

//            System.out.println("Total metrics registered: "+server.getMBeanCount());
//            System.out.println("Metric Name: "+metric);
//            System.out.println("Default domain: "+server.getDefaultDomain());
            if (server.isRegistered(metric))
            {
//                System.out.println("Metric is registered: "+metric);
                Object object = server.getAttribute(metric, "Count");
                result = (Long) object;
            }else {
//                System.out.println("Metric is not registered : "+ metric);
            }
        }
        catch (InstanceNotFoundException e)
        {
            // set to zero if the instance is not yet instantiated in MBean server.
            // this is a scenario where metrics is called before migrate or recall calls
            result = 0L;

            // also log the error
            System.out.printf("%s metric instance not found\n", name);
            e.printStackTrace();
            System.out.printf("metric instance not found : %s error: %s\n", name, e.getMessage());
        }
        return result;
    }

    protected void setMetric(MetricsOption option, MetricsResult result,MetricsKey metricsKey)
                    throws MalformedObjectNameException, AttributeNotFoundException, InstanceNotFoundException,
                    MBeanException, ReflectionException, IOException
    {
        switch (option)
        {
            case BYTES_UP:
                Long count = getCountMetrics(MetricsConstants.TotalBytesUp,metricsKey);
                result.setBytesUp(count);
                break;
            case PERSISTED_BYTES_UP:
                count = getCountMetrics(MetricsConstants.TotalPersistedBytesUp,metricsKey);
                result.setPersitedBytesUp(count);
                break;
            case TOTAL_SUCCESSFUL_PUTS:
                count = getCountMetrics(MetricsConstants.TotalSuccessfulPutRequests,metricsKey);
                result.setTotalSuccessfulPuts(count);
                break;
            case TOTAL_FAILED_PUTS:
                count = getCountMetrics(MetricsConstants.TotalFailedPutRequests,metricsKey);
                result.setTotalFailedPuts(count);
                break;
            case TOTAL_PUT_RETRIES:
                count = getCountMetrics(MetricsConstants.TotalNumberOfPutRetries,metricsKey);
                result.setTotalPutRetries(count);
                break;
            case PUT_TIME:
                count = getCountMetrics(MetricsConstants.TotalPutTime,metricsKey);
                result.setPutTime(count);
                break;
            case PERSISTED_PUT_TIME:
                count = getCountMetrics(MetricsConstants.TotalPersistedPutTime,metricsKey);
                result.setPersistedPutTime(count);
                break;
            case TOTAL_PART_ERRORS:
                count = getCountMetrics(MetricsConstants.TotalPartErrors,metricsKey);
                result.setTotalPartErrors(count);
                break;
            case PART_ERRORS_TIME:
                count = getCountMetrics(MetricsConstants.TotalPartErrorsTime,metricsKey);
                result.setPartErrorsTime(count);
                break;
            case TOTAL_PERSISTED_PARTS:
                count = getCountMetrics(MetricsConstants.TotalPartsPersisted,metricsKey);
                result.setTotalPersistedParts(count);
                break;
            case TOTAL_PARTS_PUT:
                count = getCountMetrics(MetricsConstants.TotalPartsPut,metricsKey);
                result.setTotalPartsPut(count);
                break;

            // Get Request metrics
            case BYTES_DOWN:
                count = getCountMetrics(MetricsConstants.TotalBytesDown,metricsKey);
                result.setBytesDown(count);
                break;
            case PERSISTED_BYTES_DOWN:
                count = getCountMetrics(MetricsConstants.TotalPersistedBytesDown,metricsKey);
                result.setPersitedBytesDown(count);
                break;
            case TOTAL_SUCCESSFUL_GETS:
                count = getCountMetrics(MetricsConstants.TotalSuccessfulGetRequests,metricsKey);
                result.setTotalSuccessfulGets(count);
                break;
            case TOTAL_FAILED_GETS:
                count = getCountMetrics(MetricsConstants.TotalFailedGetRequests,metricsKey);
                result.setTotalFailedGets(count);
                break;
            case TOTAL_GET_RETRIES:
                count = getCountMetrics(MetricsConstants.TotalNumberOfGetRetries,metricsKey);
                result.setTotalGetRetries(count);
                break;
            case GET_TIME:
                count = getCountMetrics(MetricsConstants.TotalGetBlobDataTime,metricsKey);
                count += getCountMetrics(MetricsConstants.TotalGetBlobTime,metricsKey);
                result.setGetTime(count);
                break;
            case PERSISTED_GET_TIME:
                count = getCountMetrics(MetricsConstants.TotalPersistedGetTime,metricsKey);
                result.setPersitedGetTime(count);
                break;
            case TOTAL_GET_ERRORS:
                count = getCountMetrics(MetricsConstants.TotalGetErrors,metricsKey);
                result.setTotalGetErrors(count);
                break;
            case GET_ERRORS_TIME:
                count = getCountMetrics(MetricsConstants.TotalGetErrorsTime,metricsKey);
                result.setGetErrorsTime(count);
                break;
            default:
                break;
        }

    }

    /**
     * It returns the metrics results based on the metrics options provided.
     * 
     * @param options
     *            List of options to collect the metrics
     * @return Returns the metrics in the result object
     * @throws Exception
     */
    public MetricsResult getMetrics(List<MetricsOption> options, MetricsKey metricsKey)
                    throws Exception
    {
        System.out.println("Collecting the monitoring metrics");
        if (options == null || options.isEmpty())
        {
            return null;
        }

        MetricsResult result = new MetricsResult();
        try
        {

            for (Iterator<MetricsOption> iterator = options.iterator(); iterator.hasNext();)
            {
                MetricsOption metricsOption = iterator.next();
//                System.out.println("Option: "+metricsOption.name());
                setMetric(metricsOption, result, metricsKey);
            }
        }
        catch (Exception e)
        {

            System.err.println("Error while metrics collection\n");
            e.printStackTrace();
            System.out.printf("Error while metrics collection : %s \n", e.getMessage());
            throw e;
        }

//        System.out.printf("Metrics = %s", result.toString());
        return result;
    }

}
