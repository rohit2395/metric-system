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
package com.rohit.monitoring;


public class MetricsResult
{

    private Long bytesUp;

    private Long persitedBytesUp;

    private Long totalSuccessfulPuts;

    private Long totalFailedPuts;

    private Long totalPutRetries;

    private Long putTime;

    private Long persistedPutTime;

    private Long totalPartErrors;

    private Long partErrorsTime;

    private Long totalPersistedParts;

    private Long totalPartsPut;

    private Long bytesDown;

    private Long persitedBytesDown;

    private Long totalSuccessfulGets;

    private Long totalFailedGets;

    private Long totalGetRetries;

    private Long getTime;

    private Long persitedGetTime;

    private Long totalGetErrors;

    private Long getErrorsTime;

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

    public Long getPersitedBytesUp()
    {
        return persitedBytesUp;
    }

    public void setPersitedBytesUp(Long persitedBytesUp)
    {
        this.persitedBytesUp = persitedBytesUp;
    }

    public Long getTotalSuccessfulPuts()
    {
        return totalSuccessfulPuts;
    }

    public void setTotalSuccessfulPuts(Long totalSuccessfulPuts)
    {
        this.totalSuccessfulPuts = totalSuccessfulPuts;
    }

    public Long getTotalFailedPuts()
    {
        return totalFailedPuts;
    }

    public void setTotalFailedPuts(Long totalFailedPuts)
    {
        this.totalFailedPuts = totalFailedPuts;
    }

    public Long getTotalPutRetries()
    {
        return totalPutRetries;
    }

    public void setTotalPutRetries(Long totalPutRetries)
    {
        this.totalPutRetries = totalPutRetries;
    }

    public Long getPutTime()
    {
        return putTime;
    }

    public void setPutTime(Long putTime)
    {
        this.putTime = putTime;
    }

    public Long getPersistedPutTime()
    {
        return persistedPutTime;
    }

    public void setPersistedPutTime(Long persistedPutTime)
    {
        this.persistedPutTime = persistedPutTime;
    }

    public Long getTotalPartErrors()
    {
        return totalPartErrors;
    }

    public void setTotalPartErrors(Long totalPartErrors)
    {
        this.totalPartErrors = totalPartErrors;
    }

    public Long getPartErrorsTime()
    {
        return partErrorsTime;
    }

    public void setPartErrorsTime(Long partErrorsTime)
    {
        this.partErrorsTime = partErrorsTime;
    }

    public Long getTotalPersistedParts()
    {
        return totalPersistedParts;
    }

    public void setTotalPersistedParts(Long totalPersistedParts)
    {
        this.totalPersistedParts = totalPersistedParts;
    }

    public Long getTotalPartsPut()
    {
        return totalPartsPut;
    }

    public void setTotalPartsPut(Long totalPartsPut)
    {
        this.totalPartsPut = totalPartsPut;
    }

    public Long getPersitedBytesDown()
    {
        return persitedBytesDown;
    }

    public void setPersitedBytesDown(Long persitedBytesDown)
    {
        this.persitedBytesDown = persitedBytesDown;
    }

    public Long getTotalSuccessfulGets()
    {
        return totalSuccessfulGets;
    }

    public void setTotalSuccessfulGets(Long totalSuccessfulGets)
    {
        this.totalSuccessfulGets = totalSuccessfulGets;
    }

    public Long getTotalFailedGets()
    {
        return totalFailedGets;
    }

    public void setTotalFailedGets(Long totalFailedGets)
    {
        this.totalFailedGets = totalFailedGets;
    }

    public Long getTotalGetRetries()
    {
        return totalGetRetries;
    }

    public void setTotalGetRetries(Long totalGetRetries)
    {
        this.totalGetRetries = totalGetRetries;
    }

    public Long getGetTime()
    {
        return getTime;
    }

    public void setGetTime(Long getTime)
    {
        this.getTime = getTime;
    }

    public Long getPersitedGetTime()
    {
        return persitedGetTime;
    }

    public void setPersitedGetTime(Long persitedGetTime)
    {
        this.persitedGetTime = persitedGetTime;
    }

    public Long getTotalGetErrors()
    {
        return totalGetErrors;
    }

    public void setTotalGetErrors(Long totalGetErrors)
    {
        this.totalGetErrors = totalGetErrors;
    }

    public Long getGetErrorsTime()
    {
        return getErrorsTime;
    }

    public void setGetErrorsTime(Long getErrorsTime)
    {
        this.getErrorsTime = getErrorsTime;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (bytesUp != null)
        {
            sb.append("bytes_up:").append(bytesUp).append("\n");
        }
        if (persitedBytesUp != null)
        {
            sb.append("persited_bytes_up:").append(persitedBytesUp).append("\n");
        }
        if (totalSuccessfulPuts != null)
        {
            sb.append("total_successful_puts:").append(totalSuccessfulPuts).append("\n");
        }
        if (totalFailedPuts != null)
        {
            sb.append("total_failed_puts:").append(totalFailedPuts).append("\n");
        }
        if (totalPutRetries != null)
        {
            sb.append("total_put_retries:").append(totalPutRetries).append("\n");
        }
        if (putTime != null)
        {
            sb.append("put_time:").append(putTime).append("\n");
        }
        if (persistedPutTime != null)
        {
            sb.append("persisted_put_time:").append(persistedPutTime).append("\n");
        }
        if (totalPartErrors != null)
        {
            sb.append("total_part_errors:").append(totalPartErrors).append("\n");
        }
        if (partErrorsTime != null)
        {
            sb.append("part_errors_time:").append(partErrorsTime).append("\n");
        }
        if (totalPersistedParts != null)
        {
            sb.append("total_persisted_parts:").append(totalPersistedParts).append("\n");
        }
        if (totalPartsPut != null)
        {
            sb.append("total_parts_put:").append(totalPartsPut).append("\n");
        }

        if (bytesDown != null)
        {
            sb.append("bytes_down:").append(bytesDown).append("\n");
        }
        if (persitedBytesDown != null)
        {
            sb.append("persited_bytes_down:").append(persitedBytesDown).append("\n");
        }
        if (totalSuccessfulGets != null)
        {
            sb.append("total_successful_gets:").append(totalSuccessfulGets).append("\n");
        }
        if (totalFailedGets != null)
        {
            sb.append("total_failed_gets:").append(totalFailedGets).append("\n");
        }
        if (totalGetRetries != null)
        {
            sb.append("total_get_retries:").append(totalGetRetries).append("\n");
        }
        if (getTime != null)
        {
            sb.append("get_time:").append(getTime).append("\n");
        }

        if (persitedGetTime != null)
        {
            sb.append("persited_get_time:").append(persitedGetTime).append("\n");
        }

        if (totalGetErrors != null)
        {
            sb.append("total_get_errors:").append(totalGetErrors).append("\n");
        }

        if (getErrorsTime != null)
        {
            sb.append("get_errors_time:").append(getErrorsTime).append("\n");
        }
        
        return sb.toString();
    }

}
