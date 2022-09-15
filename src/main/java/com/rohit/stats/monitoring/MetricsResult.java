/*******************************************************************************
 * Licensed Materials - Property of IBM
 *  
 *  OCO Source Materials
 *  
 *  (C) Copyright IBM Corp. 2015 All Rights Reserved
 *  
 *  The source code for this program is not published or other-
 *  wise divested of its trade secrets, irrespective of what has
 *  been deposited with the U.S. Copyright Office.
 *******************************************************************************/
package com.rohit.stats.monitoring;


public class MetricsResult
{

    private Long bytesUp;

    private Long putTime;

    private Long totalSuccessfulPuts;

    private Long bytesDown;

    private Long getTime;

    private Long totalSuccessfulGets;

    public Long getBytesUp()
    {
        return bytesUp;
    }

    public void setBytesUp(Long bytesUp)
    {
        this.bytesUp = bytesUp;
    }

    public Long getBytesDown()
    {
        return bytesDown;
    }

    public void setBytesDown(Long bytesDown)
    {
        this.bytesDown = bytesDown;
    }

    public Long getTotalSuccessfulPuts()
    {
        return totalSuccessfulPuts;
    }

    public void setTotalSuccessfulPuts(Long totalSuccessfulPuts)
    {
        this.totalSuccessfulPuts = totalSuccessfulPuts;
    }

    public Long getPutTime()
    {
        return putTime;
    }

    public void setPutTime(Long putTime)
    {
        this.putTime = putTime;
    }

    public Long getTotalSuccessfulGets()
    {
        return totalSuccessfulGets;
    }

    public void setTotalSuccessfulGets(Long totalSuccessfulGets)
    {
        this.totalSuccessfulGets = totalSuccessfulGets;
    }

    public Long getGetTime()
    {
        return getTime;
    }

    public void setGetTime(Long getTime)
    {
        this.getTime = getTime;
    }


    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (bytesUp != null)
        {
            sb.append("bytes_up:").append(bytesUp).append("\n");
        }
        if (totalSuccessfulPuts != null)
        {
            sb.append("total_successful_puts:").append(totalSuccessfulPuts).append("\n");
        }
        if (putTime != null)
        {
            sb.append("put_time:").append(putTime).append("\n");
        }

        if (bytesDown != null)
        {
            sb.append("bytes_down:").append(bytesDown).append("\n");
        }
        if (totalSuccessfulGets != null)
        {
            sb.append("total_successful_gets:").append(totalSuccessfulGets).append("\n");
        }
        if (getTime != null)
        {
            sb.append("get_time:").append(getTime).append("\n");
        }
        return sb.toString();
    }

}
