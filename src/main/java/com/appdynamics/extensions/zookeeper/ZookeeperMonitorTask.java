/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.zookeeper;

import com.appdynamics.extensions.zookeeper.config.Command;
import com.appdynamics.extensions.zookeeper.config.Server;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;


public class ZookeeperMonitorTask implements Callable<ZookeeperMetrics>{

    public static final Logger logger = Logger.getLogger(ZookeeperMonitor.class);
    public static final String COLON = ":";
    public static final boolean AUTO_FLUSH = true;
    public static final int SOCK_TIMEOUT_IN_MS = 10000;

    private Server server;
    //hostname
    private String host;
    //port
    private int port;
    //commands to be executed
    private Command[] commands;

    public ZookeeperMonitorTask(Server server,Command[] commands){
        this.server = server;
        this.commands = commands;
        parse(server.getServer());
    }


    public ZookeeperMetrics call() throws Exception {
        ZookeeperMetrics zMetrics = new ZookeeperMetrics();
        if (commands != null) {
            zMetrics.setDisplayName(server.getDisplayName());
            for (Command command : commands) {
                if (isCommandValid(command)) {
                    try {
                        executeCommandAndCollectMetrics(command, zMetrics);
                    }catch(Exception e){
                        logger.error("Error telnetting into server ::" + zMetrics.getDisplayName() + e);
                        //for any exception making sure that ruok is NOT_OK for that server
                        zMetrics.getMetrics().put(ZookeeperMonitorConst.RUOK,ZookeeperMonitorConst.NOT_OK);
                    }
                }
            }
        }
        return zMetrics;
    }


    private void executeCommandAndCollectMetrics(Command command, ZookeeperMetrics zMetrics) throws IOException {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        try{
            socket = createSocket();
            out = new PrintWriter(socket.getOutputStream(), AUTO_FLUSH);
            in =  new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(command.getCommand());
            String line = in.readLine();
            while(line != null){
                extractTheMetric(command, zMetrics, line);
                line = in.readLine();
            }
        }
        finally{
            if(out != null){
                out.close();
            }
            if(in != null){
                in.close();
            }
            if(socket != null){
                socket.close();
            }
        }
    }

    private Socket createSocket() throws IOException {
        Socket socket;
        socket = new Socket(host,port);
        socket.setSoTimeout(SOCK_TIMEOUT_IN_MS);  //timeout on the socket
        return socket;
    }


    private void extractTheMetric(Command command, ZookeeperMetrics zMetrics, String line) {
        String metricName = null;
        String metricValue = null;
        //separate handling for ruok
        if(isCommandRUOK(command)){
            metricValue = ZookeeperMonitorConst.NOT_OK;
            if(line.trim().equalsIgnoreCase(ZookeeperMonitorConst.IAMOK)){
                metricName = ZookeeperMonitorConst.RUOK;
                metricValue = ZookeeperMonitorConst.OK;
            }
            zMetrics.getMetrics().put(metricName,metricValue);
        }
        else{
            //metrics "key SEPARATOR value" format
            String[] splits = line.split(command.getSeparator());
            if(splits != null && splits.length > 1) {
                metricName = splits[0].trim();
                metricValue = splits[1].trim();
            }
            if(command.getFields().contains(metricName)){
                //separately handling Latecny min/avg/max metric
                if(metricName.equalsIgnoreCase(ZookeeperMonitorConst.LATENCY_MIN_AVG_MAX)){
                    handleLatencyMetrics(zMetrics, metricValue);
                }else {
                    zMetrics.getMetrics().put(metricName, metricValue);
                }
            }
        }


    }

    private void handleLatencyMetrics(ZookeeperMetrics zMetrics, String metricValue) {
        String[] latencySplits = metricValue.split("/");
        if(latencySplits != null && latencySplits.length > 2){
            zMetrics.getMetrics().put(ZookeeperMonitorConst.MIN_LATENCY,latencySplits[0]);
            zMetrics.getMetrics().put(ZookeeperMonitorConst.AVG_LATENCY,latencySplits[1]);
            zMetrics.getMetrics().put(ZookeeperMonitorConst.MAX_LATENCY,latencySplits[2]);
        }
    }

    private boolean isCommandRUOK(Command command) {
        return command.getCommand().equalsIgnoreCase(ZookeeperMonitorConst.RUOK);
    }


    private void parse(String server) {
        if(server != null){
            String[] splits = server.split(COLON);
            if(splits != null && splits.length > 1){
                host = splits[0].trim();
                try {
                    port = Integer.parseInt(splits[1].trim());
                }
                catch(NumberFormatException nfe){
                    logger.error("Error parsing the port for " + server);
                }
            }
        }
    }


    private boolean isCommandValid(Command command) {
        if(isCommandRUOK(command)){
            return true;
        }
        return command.getFields() != null && command.getFields().size() > 0;
    }
}
