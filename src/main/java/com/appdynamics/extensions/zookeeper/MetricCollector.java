/*
 *   Copyright 2020. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.zookeeper;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.zookeeper.Util.SocketUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

import static com.appdynamics.extensions.zookeeper.Util.Constants.*;

public class MetricCollector {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(MetricCollector.class);

    private Map server;
    private int socketTimeout;

    public MetricCollector(Map server, int socketTimeout){
        this.server=server;
        this.socketTimeout=socketTimeout;
    }

        protected Map<String, String> executeCommandAndCollectMetrics(String command, String separator) {
        Map<String, String> commandMetricMap = Maps.newHashMap();
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        String line;
        try {
            socket = SocketUtil.createSocket((String) server.get(HOST), (int) server.get(PORT), socketTimeout);
            out = new PrintWriter(socket.getOutputStream(), AUTO_FLUSH);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(command);

            if (RUOK.equalsIgnoreCase(command) && Strings.isNullOrEmpty(separator)) {
                line = in.readLine();
                logger.info("For server: " + server.get(DISPLAY_NAME) + ", Output of Command: " + command + " is: " + line.trim());
                commandMetricMap.put(RUOK, line.trim());
            } else {
                while ((line = in.readLine()) != null) {
                    logger.debug("For server: " + server.get(DISPLAY_NAME) + ", Output of Command: " + command + " is: " + line.trim());
                    String[] splits = line.trim().split(separator);
                    if (splits != null && splits.length > 1) {
                        if (LATENCY.equalsIgnoreCase(splits[0].trim())) {
                            commandMetricMap.putAll(extractLatencyMetrics(splits[1].trim()));
                        } else {
                            commandMetricMap.put(splits[0].trim(), splits[1].trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while extracting metrics for server " + server.get(DISPLAY_NAME), e);
        } finally {
            closeOutputStream(out);
            closeInputStream(in);
            closeSocket(socket);
        }
        return commandMetricMap;
    }

    protected Map<String, String> extractLatencyMetrics(String latencyValue) {
        Map<String, String> latencyStatMap = Maps.newHashMap();

        if (!Strings.isNullOrEmpty(latencyValue)) {
            String[] latencyValueSplit = latencyValue.split(LATENCY_VALUE_SPLITTER);
            latencyStatMap.put(LATENCY_MIN, latencyValueSplit[0].trim());
            latencyStatMap.put(LATENCY_AVG, latencyValueSplit[1].trim());
            latencyStatMap.put(LATENCY_MAX, latencyValueSplit[2].trim());
        }
        return latencyStatMap;
    }

    private void closeOutputStream(PrintWriter out) {
        if (out != null) {
            out.close();
        }
    }

    private void closeInputStream(BufferedReader in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                logger.error("Unable to close input stream ", e);
            }
        }
    }

    private void closeSocket(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Unable to close socket ", e);
            }
        }
    }
}
