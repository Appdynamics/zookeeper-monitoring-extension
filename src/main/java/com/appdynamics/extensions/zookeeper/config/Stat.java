/*
 *   Copyright 2020. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.zookeeper.config;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Stat {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "command")
    private String command;

    @XmlAttribute(name = "separator")
    private String separator;

    @XmlElement(name = "metric")
    private MetricConfig[] metric;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public MetricConfig[] getMetric() {
        return metric;
    }

    public void setMetric(MetricConfig[] metric) {
        this.metric = metric;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Stats{

        @XmlElement(name="stat")
        private Stat[] stat;

        public Stat[] getStat() {
            return stat;
        }

        public void setStat(Stat[] stat) {
            this.stat = stat;
        }
    }

}
