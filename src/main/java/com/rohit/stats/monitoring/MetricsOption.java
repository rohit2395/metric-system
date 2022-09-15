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
package com.rohit.stats.monitoring;

import java.util.HashMap;
import java.util.Map;

public enum MetricsOption
{
    //PUT request metrics
    BYTES_UP,
    TOTAL_SUCCESSFUL_PUTS,
    PUT_TIME,

    //GET request metrics
    BYTES_DOWN,
    TOTAL_SUCCESSFUL_GETS,
    GET_TIME;

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
