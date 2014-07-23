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

###Note
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
           # List of zookeeper servers
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
           
           
           #prefix used to show up metrics in AppDynamics
           metricPrefix:  "Custom Metrics|Zookeeper|"
           
           # number of concurrent tasks
           numberOfThreads: 10
           
           #timeout for the thread
           threadTimeout: 10
                    
     ```
     
       "ruok" command is the for the health check of the zookeeper server.
       


3. Configure the path to the config.yml file by editing the <task-arguments> in the monitor.xml file in the `<MACHINE_AGENT_HOME>/monitors/ZookeeperMonitor/` directory. Below is the sample

     ```
     <task-arguments>
         <!-- config file-->
         <argument name="config-file" is-required="true" default-value="monitors/ZookeeperMonitor/config.yml" />
          ....
     </task-arguments>

     ```

###Cluster level metrics : 
We support cluster level metrics only if each node in the cluster has a separate machine agent installed on it. There are two configurations required for this setup 

1. Make sure that nodes belonging to the same cluster has the same <tier-name> in the <MACHINE_AGENT_HOME>/conf/controller-info.xml, we can gather cluster level metrics.  The tier-name here should be your cluster name. 

2. Make sure that in every node in the cluster, the <MACHINE_AGENT_HOME>/monitors/ZookeeperMonitor/config.yaml should emit the same metric path. To achieve this make the displayName to be empty string and remove the trailing "|" in the metricPrefix.  The config.yaml should be something as below

```
           # List of zookeeper servers
           servers:
              - server: "localhost:2181"     #host:port
                displayName: ""
           
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
           
           
           #prefix used to show up metrics in AppDynamics
           metricPrefix:  "Custom Metrics|Zookeeper"
           
           # number of concurrent tasks
           numberOfThreads: 10
           
           #timeout for the thread
           threadTimeout: 10
                    
```

To make it more clear,assume that ZooKeeper "Node A" and ZooKeeper "Node B" belong to the same cluster "ClusterAB". In order to achieve cluster level as well as node level metrics, you should do the following
        
1. Both Node A and Node B should have separate machine agents installed on them. Both the machine agent should have their own ZooKeeper extension.
    
2. In the Node A's and Node B's machine agents' controller-info.xml make sure that you have the tier name to be your cluster name , "ClusterAB" here. Also, nodeName in controller-info.xml is "Node A" and "Node B" resp.
        
3. The config.yaml for Node A and Node B should be

```
        
        # List of zookeeper servers
                   servers:
                      - server: "localhost:2181"     #host:port
                        displayName: ""
                   
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
                   
                   
                   #prefix used to show up metrics in AppDynamics
                   metricPrefix:  "Custom Metrics|Zookeeper"
                   
                   # number of concurrent tasks
                   numberOfThreads: 10
                   
                   #timeout for the thread
                   threadTimeout: 10
        
```      

        
Now, if Node A and Node B are reporting say a metric called ReadLatency to the controller, with the above configuration they will be reporting it using the same metric path.
        
Node A reports Custom Metrics | ClusterAB | ReadLatency = 50 
Node B reports Custom Metrics | ClusterAB | ReadLatency = 500
        
The controller will automatically average out the metrics at the cluster (tier) level as well. So you should be able to see the cluster level metrics under
        
Application Performance Management | Custom Metrics | ClusterAB | ReadLatency = 225
        
Also, now if you want to see individual node metrics you can view it under
        
Application Performance Management | Custom Metrics | ClusterAB | Individual Nodes | Node A | ReadLatency = 50 
Application Performance Management | Custom Metrics | ClusterAB | Individual Nodes | Node B | ReadLatency = 500



Please note that for now the cluster level metrics are obtained by the averaging all the individual node level metrics in a cluster.



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
