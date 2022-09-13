/*******************************************************************************
 * Licensed Materials - Property of IBM
 * 
 * OCO Source Materials
 * 
 * (C) Copyright IBM Corp. 2015 All Rights Reserved
 * 
 * The source code for this program is not published or other-
 * wise divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *******************************************************************************/
package com.rohit.monitoring;

import java.util.HashMap;
import java.util.Map;

public enum MetricsOption
{
    //PUT request metrics
    BYTES_UP,
    PERSISTED_BYTES_UP,
    TOTAL_SUCCESSFUL_PUTS,
    TOTAL_FAILED_PUTS,
    TOTAL_PUT_RETRIES,
    PUT_TIME,
    PERSISTED_PUT_TIME,
    TOTAL_PART_ERRORS,
    PART_ERRORS_TIME,
    TOTAL_PERSISTED_PARTS,
    TOTAL_PARTS_PUT,

    //GET request metrics
    BYTES_DOWN,
    PERSISTED_BYTES_DOWN,
    TOTAL_SUCCESSFUL_GETS,
    TOTAL_FAILED_GETS,
    TOTAL_GET_RETRIES,
    GET_TIME,
    PERSISTED_GET_TIME,
    TOTAL_GET_ERRORS,
    GET_ERRORS_TIME;

    private static final Map<String, MetricsOption> optionMap = new HashMap<String, MetricsOption>();
    static
    {
        for (MetricsOption op : MetricsOption.values())
        {
            optionMap.put(op.name().toLowerCase(), op);
        }
    }

    public static MetricsOption getOption(String option)
    {
        return optionMap.get(option);

    }
}
