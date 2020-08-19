/*
 *   Copyright 2020. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.zookeeper.config;

import com.google.common.collect.Maps;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
public class MetricConfig {

    @XmlAttribute(name = "attr")
    private String attr;

    @XmlAttribute(name = "alias")
    private String alias;

    @XmlAttribute(name = "aggregationType")
    private String aggregationType;

    @XmlAttribute(name = "timeRollUpType")
    private String timeRollUpType;

    @XmlAttribute(name = "clusterRollUpType")
    private String clusterRollUpType;

    @XmlAttribute(name = "multiplier")
    private String multiplier;

    @XmlAttribute(name = "delta")
    private String delta;

    @XmlElement(name="convert")
    private MetricConverter[] convert;

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAggregationType() {
        return aggregationType;
    }

    public void setAggregationType(String aggregationType) {
        this.aggregationType = aggregationType;
    }

    public String getTimeRollUpType() {
        return timeRollUpType;
    }

    public void setTimeRollUpType(String timeRollUpType) {
        this.timeRollUpType = timeRollUpType;
    }

    public String getClusterRollUpType() {
        return clusterRollUpType;
    }

    public void setClusterRollUpType(String clusterRollUpType) {
        this.clusterRollUpType = clusterRollUpType;
    }

    public String getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(String multiplier) {
        this.multiplier = multiplier;
    }

    public String getDelta() {
        return delta;
    }

    public void setDelta(String delta) {
        this.delta = delta;
    }

    public Map<String,String> getConvert() {
        Map<String,String> converterMap = Maps.newHashMap();
        if(convert != null && convert.length>0){
            return generateConverterMap(converterMap);
        }
        return converterMap;
    }

    private Map<String,String> generateConverterMap(Map<String,String> converterMap){
        for(MetricConverter converter: convert){
            converterMap.put(converter.getLabel(),converter.getValue());
        }
        return converterMap;
    }

    public void setConvert(MetricConverter[] convert) {
        this.convert = convert;
    }
}
