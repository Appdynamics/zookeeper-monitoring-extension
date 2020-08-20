/*
 *   Copyright 2020. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.zookeeper;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;

import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.zookeeper.config.MetricConfig;
import com.appdynamics.extensions.zookeeper.config.Stat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.appdynamics.extensions.zookeeper.Util.Constants.*;

public class ZookeeperMonitorTask implements AMonitorTaskRunnable {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ZookeeperMonitorTask.class);

    private Map<String,?> server;
    private MetricWriteHelper metricWriteHelper;
    private MonitorContextConfiguration contextConfiguration;
    private BigInteger ruokValue = BigInteger.ZERO;
    private String metricPrefix;
    private ObjectMapper objectMapper = new ObjectMapper();
    private MetricCollector metricCollector;


    public ZookeeperMonitorTask(MonitorContextConfiguration contextConfiguration, MetricWriteHelper metricWriteHelper, Map<String,?> server, MetricCollector metricCollector) {
        this.contextConfiguration = contextConfiguration;
        this.metricWriteHelper = metricWriteHelper;
        this.metricCollector = metricCollector;
        this.server = server;
        this.metricPrefix = contextConfiguration.getMetricPrefix() + SEPARATOR + server.get(DISPLAY_NAME) + SEPARATOR;

    }

    @Override
    public void run() {
        Stat.Stats stats = (Stat.Stats) contextConfiguration.getMetricsXml();
        CopyOnWriteArrayList<Metric> metricList = new CopyOnWriteArrayList<>();
        try {
            Map<String, String> ruokCheck = metricCollector.executeCommandAndCollectMetrics(RUOK, "");
            if (ruokCheck != null && !ruokCheck.isEmpty() && (ruokCheck.get(RUOK)).equalsIgnoreCase(IMOK)) {
                ruokValue = BigInteger.ONE;
                for (Stat stat : stats.getStat()) {
                    logger.debug("Extracting metrics from command: "+stat.getCommand()+" for server: "+server.get(DISPLAY_NAME));
                    Map<String, String> metricMap = metricCollector.executeCommandAndCollectMetrics(stat.getCommand(), stat.getSeparator());
                    if(!metricMap.isEmpty()){
                        metricList.addAll(extractMetrics(metricMap, stat.getMetric(), stat.getCommand()));
                    }else{
                        logger.debug("For Server {} either {} command output is null or command is invalid",server.get(DISPLAY_NAME),stat.getCommand());
                    }
                }
            } else {
                logger.info("Not extracting metrics for server: " + server.get(DISPLAY_NAME) + " as RUOK command output is either null or invalid");
            }
        } catch (Exception e) {
            logger.error("Error occurred while running task for server: " + server.get(DISPLAY_NAME), e);
        } finally {
            metricList.add(new Metric(RUOK, ruokValue.toString(), metricPrefix + RUOK));
            metricWriteHelper.transformAndPrintMetrics(metricList);
        }
    }

    protected List<Metric> extractMetrics(Map<String, String> metricMap, MetricConfig[] metricConfigs, String command) {
        List<Metric> metricDataList = Lists.newArrayList();

        if (metricConfigs != null && metricConfigs.length > 0) {
            for (MetricConfig metric : metricConfigs) {
                String value = metricMap.get(metric.getAttr());
                if (!Strings.isNullOrEmpty(value)) {
                    metricDataList.add(new Metric(metric.getAttr(), value, metricPrefix + command + SEPARATOR + metric.getAlias(), objectMapper.convertValue(metric, Map.class)));
                }
            }
        } else{
            logger.debug("Not printing metrics for "+command+" command as it is not configured in metrics file");
        }
        return metricDataList;
    }

    @Override
    public void onTaskComplete() {
        logger.info("Completed Zookeeper Monitor task for " + server.get(DISPLAY_NAME));
    }
}
