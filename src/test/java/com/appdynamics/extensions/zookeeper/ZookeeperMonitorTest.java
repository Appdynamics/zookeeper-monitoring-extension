package com.appdynamics.extensions.zookeeper;

import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Test;

import java.util.Map;

public class ZookeeperMonitorTest {

    private static final int NUMBER_OF_THREADS = 1;
    public static final String CONFIG_ARG = "config-file";

    @Test
    public void testZookeeperMonitorExtension() throws TaskExecutionException {
        ZookeeperMonitor zookeeper = new ZookeeperMonitor();
        Map<String,String> taskArgs = Maps.newHashMap();
        taskArgs.put(CONFIG_ARG, "src/test/resources/conf/config.yml");
        zookeeper.execute(taskArgs,null);
    }
}
