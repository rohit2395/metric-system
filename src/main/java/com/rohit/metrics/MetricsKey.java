package com.rohit.metrics;

public class MetricsKey  implements Comparable<MetricsKey> {

    private String container;
    private String csap;

    public static final String DEFAULT_KEY_VAL = "DEFAULT";

    /**
     * Default constructor to set the default values
     */
    public MetricsKey() {
        container = DEFAULT_KEY_VAL;  // SVC / DS8K will use this if they don't
        csap = DEFAULT_KEY_VAL;       //   specify anything
    }

    public MetricsKey(String container, String csap) {
        this.container = container;
        this.csap = csap;
    }

    public String getContainer() { return container; }

    public String getCsap() { return csap; }

    public void setContainer(String container) { this.container = container; }

    public void setCsap(String csap) { this.csap = csap; }

    @Override
    public int compareTo(MetricsKey that) {
        if (that == null)
            throw new NullPointerException();

        int comp;
        if ((comp = ((Comparable<String>) this.getCsap()).compareTo(that.getCsap())) != 0)
            return comp;
        if ((comp = ((Comparable<String>) this.getContainer()).compareTo(that.getContainer())) != 0)
            return comp;

        return 0;
    }

    @Override
    public String toString() {
        return "container=" + this.getContainer() + ",csap=" + this.getCsap();
    }

}
