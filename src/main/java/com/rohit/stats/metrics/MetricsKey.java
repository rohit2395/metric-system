package com.rohit.stats.metrics;

import com.rohit.stats.metrics.interfaces.MetricsConstants;

public class MetricsKey  implements Comparable<MetricsKey> {

    private String keyName;


    /**
     * Default constructor to set the default values
     */
    public MetricsKey() {
        keyName = MetricsConstants.DEFAULT;
    }

    public MetricsKey(String container) {
        this.keyName = container;
    }

    public String getKeyName() { return keyName; }


    public void setKeyName(String keyName) { this.keyName = keyName; }


    @Override
    public int compareTo(MetricsKey that) {
        if (that == null)
            throw new NullPointerException();

        int comp;
        if ((comp = ((Comparable<String>) this.getKeyName()).compareTo(that.getKeyName())) != 0)
            return comp;

        return 0;
    }

    @Override
    public String toString() {
        return "keyName=" + this.getKeyName();
    }

}
