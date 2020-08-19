package com.appdynamics.extensions.zookeeper;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.zookeeper.config.Stat;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MetricCollectorTest {

    private Map<String,?> server;

    private int socketTimeout;

    private MonitorContextConfiguration contextConfiguration = new MonitorContextConfiguration("ZookeeperMonitor","Custom Metrics|Zookeeper", Mockito.mock(File.class),Mockito.mock(AMonitorJob.class));

    private MetricCollector spyMetricCollector;

    @Before
    public void setup(){

        contextConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        contextConfiguration.setMetricXml("src/test/resources/conf/metrics.xml", Stat.Stats.class);

        Map<String,?> configyml = contextConfiguration.getConfigYml();
        server = ((List<Map<String,?>>) configyml.get("servers")).get(0);
        socketTimeout = (int)((Map)configyml.get("connection")).get("socketTimeout");

        spyMetricCollector = Mockito.spy(new MetricCollector(server,socketTimeout));
    }

    @Test
    public void extractLatencyMetricsTest(){

        Map<String,String> expLatencyMap = expectedLatencyMap();

        String latencyMetricValue = "3/5/2";
        Map<String,String> latencyMetricMap = spyMetricCollector.extractLatencyMetrics(latencyMetricValue);

        Assert.assertTrue(latencyMetricMap.equals(expLatencyMap));

    }

    private Map<String,String> expectedLatencyMap(){
        Map<String,String> expectedLatencyMap = Maps.newHashMap();
        expectedLatencyMap.put("Latency|Min","3");
        expectedLatencyMap.put("Latency|Avg","5");
        expectedLatencyMap.put("Latency|Max","2");
        return expectedLatencyMap;
    }

}
