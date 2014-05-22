zookeeper-monitoring-extension
==============================
An AppDynamics extension to be used with a stand alone Java machine agent to provide metrics for Zookeeper servers.


## Metrics Provided ##

The list of metrics provided is self-configurable. The metrics are extracted by running the commands listed in the [Zookeeper Documentation].
The commands and the fields to be extracted can be configured in the config.yml file mentioned below.

We also send "ruok" with a value -1 when an error occurs and 1 when the metrics collection is successful.


## Installation ##

1. Run "mvn clean install" and find the ZookeeperMonitor.zip file in the "target" folder. You can also download the ZookeeperMonitor.zip from [AppDynamics Exchange][].
2. Unzip as "ZookeeperMonitor" and copy the "ZookeeperMonitor" directory to `<MACHINE_AGENT_HOME>/monitors`.



## Configuration ##

   ### Note:
       Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a yaml validator http://yamllint.com/

1. Configure the zookeeper instances by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/ZookeeperMonitor/`.
2. Configure the zookeeper commands in the config.yml file. Depending on the version of Zookeeper, you can run either "mntr" or "stat" or any other command. Please make sure you provide the right separator to parse the metric key and value.
 
     For eg. "stat" command returns the following
     ```
         Latency min/avg/max: 0/0/0
         Received: 87
         Sent: 86
         Connections: 1
         Outstanding: 0
         Node count: 4
     ```
      You can configure the fields to be extracted and the separator to be used (":" here). Below is a sample config.yml file.
     
      
       ```
           servers:
             - server: "localhost:2181"     #host:port
               displayName: zh1
             - server: ""
               displayName: zh2
          
          
           # The list of commands can be found here http://zookeeper.apache.org/doc/r3.4.6/zookeeperAdmin.html#sc_zkCommands
          
           commands:
              - command: "ruok"
              - command: "stat"
                separator: ":"
                fields: [
                   Received,
                   Sent,
                   Outstanding,
                   Node count,
                   Latency min/avg/max
                ]
          
           # Uncomment the following to support additional metrics
           #   - command: "mntr"
           #     separator: "\t"
           #     fields: [
           #       zk_avg_latency,
           #       zk_max_latency,
           #       zk_min_latency,
           #       zk_packets_received,
           #       zk_packets_sent,
           #       zk_num_alive_connections,
           #       zk_outstanding_requests,
           #       zk_znode_count,
           #       zk_watch_count,
           #       zk_ephemerals_count,
           #       zk_approximate_data_size,
           #       zk_followers,                      #only exposed by the Leader
           #       zk_synced_followers,               #only exposed by the Leader
           #       zk_pending_syncs,                  #only exposed by the Leader
           #       zk_open_file_descriptor_count,     #only available on Unix platforms
           #       zk_max_file_descriptor_count       #only available on Unix platforms
           #     ]
          
          
          
           metricPrefix:  "Custom Metrics|Zookeeper|"
           
     ```
     
       "ruok" command is the for the health check of the zookeeper server.
       Please make sure you indent your config.yml file with spaces. You can follow the Yaml tutorial here    http://ess.khhq.net/wiki/YAML_Tutorial.


3. Configure the path to the config.yml file by editing the <task-arguments> in the monitor.xml file in the `<MACHINE_AGENT_HOME>/monitors/ZookeeperMonitor/` directory. Below is the sample

     ```
     <task-arguments>
         <!-- config file-->
         <argument name="config-file" is-required="true" default-value="monitors/ZookeeperMonitor/config.yml" />
          ....
     </task-arguments>

     ```



## Custom Dashboad ##
![](https://raw.githubusercontent.com/Appdynamics/zookeeper-monitoring-extension/master/zookeeper.png)

## Contributing ##

Always feel free to fork and contribute any changes directly via [GitHub][].

## Community ##

Find out more in the [AppDynamics Exchange][].

## Support ##

For any questions or feature request, please contact [AppDynamics Center of Excellence][].

**Version:** 1.0.0
**Controller Compatibility:** 3.7+


[Github]: https://github.com/Appdynamics/zookeeper-monitoring-extension
[AppDynamics Exchange]: http://community.appdynamics.com/t5/AppDynamics-eXchange/idb-p/extensions
[AppDynamics Center of Excellence]: mailto:ace-request@appdynamics.com
[Zookeeper Documentation]: http://zookeeper.apache.org/doc/r3.4.6/zookeeperAdmin.html#sc_zkCommands
