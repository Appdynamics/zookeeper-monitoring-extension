package com.appdynamics.extensions.zookeeper;


import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.zookeeper.config.ConfigUtil;
import com.appdynamics.extensions.zookeeper.config.Configuration;
import com.appdynamics.extensions.zookeeper.config.Server;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * This extension will telnet into each zookeeper node and extract out the metrics.
 */
public class ZookeeperMonitor extends AManagedMonitor {

    public static final Logger logger = Logger.getLogger(ZookeeperMonitor.class);
    public static final String CONFIG_ARG = "config-file";
    public static final String METRIC_SEPARATOR = "|";
    public static final String LOG_PREFIX = "log-prefix";
    private static final int DEFAULT_NUMBER_OF_THREADS = 10;
    public static final int DEFAULT_THREAD_TIMEOUT = 10;

    private ExecutorService threadPool;
    private static String logPrefix;

    //To load the config files
    private final static ConfigUtil<Configuration> configUtil = new ConfigUtil<Configuration>();


    public ZookeeperMonitor() {
        String msg = "Using Monitor Version [" + getImplementationVersion() + "]";
        logger.info(msg);
        System.out.println(msg);
    }


    public TaskOutput execute(Map<String, String> taskArgs, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        if (taskArgs != null) {
            setLogPrefix(taskArgs.get(LOG_PREFIX));
            logger.info(getLogPrefix() + "Starting the Zookeper Monitoring task.");
            if (logger.isDebugEnabled()) {
                logger.debug(getLogPrefix() + "Task Arguments Passed ::" + taskArgs);
            }
            String configFilename = getConfigFilename(taskArgs.get(CONFIG_ARG));
            try {
                //read the config.
                Configuration config = configUtil.readConfig(configFilename, Configuration.class);
                threadPool = Executors.newFixedThreadPool(config.getNumberOfThreads() == 0 ? DEFAULT_NUMBER_OF_THREADS : config.getNumberOfThreads());
                //create parallel tasks to telnet into each server
                List<Future<ZookeeperMetrics>> parallelTasks = createParallelTasks(config);
                //collect the metrics
                List<ZookeeperMetrics> zMetrics = collectMetrics(parallelTasks,config.getThreadTimeout() == 0 ? DEFAULT_THREAD_TIMEOUT : config.getThreadTimeout());
                //print the metrics
                printStats(config, zMetrics);
                logger.info(getLogPrefix() + "Zookeeper monitoring task completed successfully.");
                return new TaskOutput(getLogPrefix() + "Zookeeper monitoring task completed successfully.");
            } catch (FileNotFoundException e) {
                logger.error(getLogPrefix() + "Config file not found :: " + configFilename, e);
            } catch (Exception e) {
                logger.error(getLogPrefix() + "Metrics collection failed", e);
            }
        }
        throw new TaskExecutionException(getLogPrefix() + "Zookeeper monitoring task completed with failures.");
    }



    private List<Future<ZookeeperMetrics>> createParallelTasks(Configuration config) {
        List<Future<ZookeeperMetrics>> parallelTasks = new ArrayList<Future<ZookeeperMetrics>>();
        if (config != null && config.getServers() != null) {
            for (Server server : config.getServers()) {
                ZookeeperMonitorTask zookeeperTask = new ZookeeperMonitorTask(server,config.getCommands());
                parallelTasks.add(getThreadPool().submit(zookeeperTask));
            }
        }
        return parallelTasks;
    }


    private List<ZookeeperMetrics> collectMetrics(List<Future<ZookeeperMetrics>> parallelTasks,int timeout) {
        List<ZookeeperMetrics> allMetrics = new ArrayList<ZookeeperMetrics>();
        for (Future<ZookeeperMetrics> aParallelTask : parallelTasks) {
            ZookeeperMetrics zMetrics = null;
            try {
                zMetrics = aParallelTask.get(10, TimeUnit.SECONDS);
                allMetrics.add(zMetrics);
            } catch (InterruptedException e) {
                logger.error(getLogPrefix() + "Task interrupted." + e);
            } catch (ExecutionException e) {
                logger.error(getLogPrefix() + "Task execution failed." + e);
            } catch (TimeoutException e) {
                logger.error(getLogPrefix() + "Task timed out." + e);
            }
        }
        return allMetrics;
    }


    private void printStats(Configuration config, List<ZookeeperMetrics> zMetrics) {
        for (ZookeeperMetrics zMetric : zMetrics) {
            StringBuffer metricPath = new StringBuffer();
            metricPath.append(config.getMetricPrefix()).append(zMetric.getDisplayName()).append(METRIC_SEPARATOR);
            Map<String,String> metricsForAServer = zMetric.getMetrics();
            Iterator<String> it = metricsForAServer.keySet().iterator();
            while(it.hasNext()) {
                String metricName = it.next();
                String metricValue = metricsForAServer.get(metricName);
                printCollectiveObservedCurrent(metricPath.toString() + metricName,metricValue);
            }
        }
    }



    private void printCollectiveObservedCurrent(String metricPath, String metricValue) {
        printMetric(metricPath, metricValue,
                MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
        );
    }

    /**
     * A helper method to report the metrics.
     * @param metricPath
     * @param metricValue
     * @param aggType
     * @param timeRollupType
     * @param clusterRollupType
     */
    private void printMetric(String metricPath,String metricValue,String aggType,String timeRollupType,String clusterRollupType){
        MetricWriter metricWriter = getMetricWriter(metricPath,
                aggType,
                timeRollupType,
                clusterRollupType
        );
       //   System.out.println(getLogPrefix()+"Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
       //            + "] metric = " + metricPath + " = " + metricValue);
        if (logger.isDebugEnabled()) {
            logger.debug(getLogPrefix() + "Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
                    + "] metric = " + metricPath + " = " + metricValue);
        }
        metricWriter.printMetric(metricValue);
    }



    /**
     * Returns a config file name,
     * @param filename
     * @return String
     */
    private String getConfigFilename(String filename) {
        if(filename == null){
            return "";
        }
        //for absolute paths
        if(new File(filename).exists()){
            return filename;
        }
        //for relative paths
        File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
        String configFileName = "";
        if(!Strings.isNullOrEmpty(filename)){
            configFileName = jarPath + File.separator + filename;
        }
        return configFileName;
    }


    public static String getImplementationVersion() {
        return ZookeeperMonitor.class.getPackage().getImplementationTitle();
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    public void setLogPrefix(String logPrefix) {
        this.logPrefix = (logPrefix != null) ? logPrefix : "";
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

}
