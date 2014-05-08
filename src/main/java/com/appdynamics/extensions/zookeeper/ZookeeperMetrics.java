package com.appdynamics.extensions.zookeeper;


import java.util.HashMap;
import java.util.Map;

public class ZookeeperMetrics {

    private String displayName;
    private Map<String,String> metrics = new HashMap<String,String>();

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<String, String> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, String> metrics) {
        this.metrics = metrics;
    }
}
