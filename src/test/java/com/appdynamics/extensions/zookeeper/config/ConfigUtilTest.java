package com.appdynamics.extensions.zookeeper.config;


import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;

public class ConfigUtilTest {

    ConfigUtil<Configuration> configUtil = new ConfigUtil<Configuration>();

    @Test
    public void loadConfigSuccessfully() throws FileNotFoundException {
        Configuration configuration = configUtil.readConfig(this.getClass().getResource("/conf/config.yaml").getFile(),Configuration.class);
        Assert.assertTrue(configuration != null);
    }
}
