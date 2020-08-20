/*
 *   Copyright 2020. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.zookeeper;

import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Test;

import java.util.Map;

public class ZookeeperMonitorTest {

    public static final String CONFIG_ARG = "config-file";
    public static final String METRIC_ARG = "metric-file";

    @Test
    public void testZookeeperMonitorExtension() throws TaskExecutionException {
        ZookeeperMonitor zookeeper = new ZookeeperMonitor();
        Map<String,String> taskArgs = Maps.newHashMap();
        taskArgs.put(CONFIG_ARG, "src/test/resources/conf/config.yml");
        taskArgs.put(METRIC_ARG, "src/test/resources/conf/metrics.xml");
        zookeeper.execute(taskArgs,null);
    }
}
