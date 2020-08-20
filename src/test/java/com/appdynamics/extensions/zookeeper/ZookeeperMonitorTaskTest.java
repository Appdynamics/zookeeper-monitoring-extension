package com.appdynamics.extensions.zookeeper;


import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.zookeeper.Util.SocketUtil;
import com.appdynamics.extensions.zookeeper.config.MetricConfig;
import com.appdynamics.extensions.zookeeper.config.Stat;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SocketUtil.class)
@PowerMockIgnore("javax.net.ssl.*")
public class ZookeeperMonitorTaskTest {

    @Mock
    MetricWriteHelper metricWriteHelper;

    @Mock
    MetricCollector metricCollector;

    private Map<String,?> server;

    private MonitorContextConfiguration contextConfiguration = new MonitorContextConfiguration("ZookeeperMonitor","Custom Metrics|Zookeeper",Mockito.mock(File.class),Mockito.mock(AMonitorJob.class));

    ZookeeperMonitorTask spyTask;


    @Before
    public void setup(){

        PowerMockito.mockStatic(SocketUtil.class);

        contextConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        contextConfiguration.setMetricXml("src/test/resources/conf/metrics.xml", Stat.Stats.class);

        Map<String,?> configyml = contextConfiguration.getConfigYml();
        server = ((List<Map<String,?>>) configyml.get("servers")).get(0);

        spyTask = Mockito.spy(new ZookeeperMonitorTask(contextConfiguration,metricWriteHelper, server, metricCollector));

    }

    @Test
    public void runTest() throws IOException {

        Map<String,String> expectedValueMap = expectedMetrics();

        Map<String,String> resultMap = Maps.newHashMap();

        ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);

        Map<String,String> ruokCommandMap = Maps.newHashMap();
        ruokCommandMap.put("ruok","imok");

        Mockito.when(metricCollector.executeCommandAndCollectMetrics("ruok","")).thenReturn(ruokCommandMap);

        Mockito.when(metricCollector.executeCommandAndCollectMetrics("stat",":")).thenAnswer(
                new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                        Map<String,String> statMap = Maps.newHashMap();
                        BufferedReader br = new BufferedReader(new FileReader(new File("src/test/resources/statcommandresponse")));

                        String line;

                        while((line = br.readLine())!=null){
                            String[] splits = line.split(":");
                            if(splits.length>1){
                                statMap.put(splits[0].trim(),splits[1].trim());
                            }
                        }
                        br.close();
                        return statMap;
                    }
                }
        );

        spyTask.run();

        Mockito.verify(metricWriteHelper).transformAndPrintMetrics(pathCaptor.capture());

        for(Metric metric: (List<Metric>)pathCaptor.getValue()){
            resultMap.put(metric.getMetricPath(),metric.getMetricValue());
        }

        Assert.assertTrue(resultMap.equals(expectedValueMap));

    }

    private Map<String,String> expectedMetrics(){
        Map<String,String> valueMap = Maps.newHashMap();
        valueMap.put("Custom Metrics|Zookeeper|zh1|stat|Received","1");
        valueMap.put("Custom Metrics|Zookeeper|zh1|stat|Sent","0");
        valueMap.put("Custom Metrics|Zookeeper|zh1|stat|Connections","1");
        valueMap.put("Custom Metrics|Zookeeper|zh1|ruok","1");
        return valueMap;
    }

    @Test
    public void extractMetricsTest(){

        List<String> resultList = Lists.newArrayList();
        resultList.add("Custom Metrics|Zookeeper|zh1|stat|Received");
        resultList.add("Custom Metrics|Zookeeper|zh1|stat|Sent");
        resultList.add("Custom Metrics|Zookeeper|zh1|stat|Connections");

        ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);

        Map<String,String> metricMap = Maps.newHashMap();
        metricMap.put("Received","3");
        metricMap.put("Sent","2");
        metricMap.put("Connections","4");
        metricMap.put("Outstanding","1");

        Stat.Stats stat = (Stat.Stats)contextConfiguration.getMetricsXml();
        MetricConfig[] metricConfig = (stat.getStat()[0]).getMetric();

        List<Metric> dataList = spyTask.extractMetrics(metricMap,metricConfig,"stat");

        for(Metric m: dataList){
            System.out.println(m);
            Assert.assertTrue(resultList.contains(m.getMetricPath()));
        }
    }

}
