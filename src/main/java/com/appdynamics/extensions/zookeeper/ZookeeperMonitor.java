/*
 *   Copyright 2020. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.zookeeper;


import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.extensions.zookeeper.config.Stat;
import com.google.common.collect.Maps;
import org.slf4j.Logger;

import java.util.*;

import static com.appdynamics.extensions.zookeeper.Util.Constants.*;

/**
 * This extension will telnet into each zookeeper node and extract out the metrics.
 */
public class ZookeeperMonitor extends ABaseMonitor {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ZookeeperMonitor.class);

    private MonitorContextConfiguration contextConfiguration;
    private Map<String,?> configYml = Maps.newHashMap();

    @Override
    protected String getDefaultMetricPrefix() {
        return DEFAULT_METRIC_PREFIX;
    }

    @Override
    public String getMonitorName() {
        return MONITOR_NAME;
    }

    @Override
    protected void initializeMoreStuff(Map<String, String> args) {
        contextConfiguration = getContextConfiguration();
        configYml = contextConfiguration.getConfigYml();
        AssertUtils.assertNotNull(configYml,"The config.yml is not available");
        contextConfiguration.setMetricXml(args.get(METRIC_FILE), Stat.Stats.class);
    }

    @Override
    protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {
        List<Map<String,?>> servers = (List<Map<String, ?>>) configYml.get(SERVERS);
        int socketTimeout = (int) ((Map)configYml.get(CONNECTION)).get(SOCKET_TIMEOUT);
        for(Map<String,?> server: servers){
            MetricCollector metricCollector = new MetricCollector(server,socketTimeout);
            String name = (String) server.get(DISPLAY_NAME);
            AssertUtils.assertNotNull(server,"The server arguments cannot be empty ");
            AssertUtils.assertNotNull(server.get("name"),"The name cannot be null");
            logger.info("Starting monitoring task for server "+name);
            ZookeeperMonitorTask task = new ZookeeperMonitorTask(contextConfiguration,tasksExecutionServiceProvider.getMetricWriteHelper(),server, metricCollector);
            tasksExecutionServiceProvider.submit(name,task);
        }
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        return (List<Map<String, ?>>) configYml.get(SERVERS);
    }

}
