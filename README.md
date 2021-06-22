zookeeper-monitoring-extension
==============================

## Use Case
An AppDynamics extension to be used with a stand alone Java machine agent to provide metrics for Zookeeper servers. This extensions uses 4 letter zookeeper command (stat, mntr) and extract metrics from the server.


## Prerequisite
1. Before the extension is installed, the prerequisites mentioned [here](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) need to be met. Please do not proceed with the extension installation if the specified prerequisites are not met.
2. The Extension also needs a [zookeeper](https://zookeeper.apache.org/) server to be installed.
3. The extension needs to be able to connect to Zookeeper in order to collect and send metrics. To do this, you will have to either establish a remote connection in between the extension and the product, or have an agent on the same machine running the product in order for the extension to collect and send the metrics.

## Installation ##

1. Run "mvn clean install" and find the ZookeeperMonitor.zip file in the "target" folder. You can also download the ZookeeperMonitor.zip from [AppDynamics Exchange](https://www.appdynamics.com/community/exchange/extension/zookeeper-monitoring-extension/).
2. Please place the extension in the "monitors" directory of your Machine Agent installation directory. Do not place the extension in the "extensions" directory of your Machine Agent installation directory.

## Configuration ##

#### Config.yml
##### Note: Please make sure to not use tab (\t) while editing yml files.

1. Configure the zookeeper instances by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/ZookeeperMonitor/`.
    ```
        servers:
          - host: "localhost"
            port: 2181
            name: "zh1"
    ```
    Provide zookeeper host, port and display name for your zookeeper server.

2. You can configure multiple zookeeper servers like below:
    ```
        servers:
          - host: "localhost"
            port: 2181
            name: "zh1"
          - host: "localhost"
            port: 2182
            name: "zh2"   
    ```
    Please make sure to provide unique display names if multiple servers are configured.

##### Number Of Threads
Always include one thread per server + 1.

For example, if you have 4 zookeeper servers configured, then number of threads required are 5 (4 thread per server + 1 to run main task)     

##### Yml Validation
Please copy all the contents of the config.yml file and go to http://www.yamllint.com/ . On reaching the website, paste the contents and press the “Go” button on the bottom left.

#### Metrics.xml

You can add/remove metrics of your choice by modifying the provided metrics.xml file. Please look at how the metrics have been defined and follow the same convention when adding new metrics. You do have the ability to also chose your Rollup types as well as for each metric as well as set an alias name that you would like displayed on the metric browser.

#### Metrics
The list of metrics provided is self-configurable. The metrics are extracted by running the commands listed in the [Zookeeper Documentation](http://zookeeper.apache.org/doc/r3.4.6/zookeeperAdmin.html#sc_zkCommands).
The commands and the fields to be extracted can be configured in the config.yml file mentioned below.

We also send "ruok" with a value 0 when an error occurs and 1 when the metrics collection is successful.

#### Cluster level metrics : 

We support cluster level metrics only if each node in the cluster has a separate machine agent installed on it. There are two configurations required for this setup 

1. Make sure that nodes belonging to the same cluster has the same <tier-name> in the <MACHINE_AGENT_HOME>/conf/controller-info.xml, we can gather cluster level metrics.  The tier-name here should be your cluster name. 

2. Make sure that in every node in the cluster, the <MACHINE_AGENT_HOME>/monitors/ZookeeperMonitor/config.yml should emit the same metric path.

## Credentials Encryption ##

Please visit [Encryption Guidelines](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.
If you want to use password encryption, please send arguments as connectionProperties. You will have to fill in the encrypted Password and Encryption Key fields in the config but you will also have to give an empty "" value to the password field and the encrypted password will be automatically picked up.

## Extensions Workbench

Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-use-the-Extensions-WorkBench/ta-p/30130).

## Troubleshooting

Please follow the steps listed in this [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension. If these don't solve your issue, please follow the last step on the [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695) to contact the support team.

## Support Tickets

If after going through the [Troubleshooting Document](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695) you have not been able to get your extension working, please file a ticket with the following information:

1. Stop the running machine agent .
2. Delete all existing logs under <MachineAgent>/logs .
3. Please enable debug logging by editing the file <MachineAgent>/conf/logging/log4j.xml. Change the level value of the following <logger> elements to debug. 
    ```
    <logger name="com.singularity">
    <logger name="com.appdynamics">
   ```
4. Start the machine agent and please let it run for 10 mins. Then zip and upload all the logs in the directory <MachineAgent>/logs/*.
Attach the zipped <MachineAgent>/conf/* directory.
5. Attach the zipped <MachineAgent>/monitors/ExtensionFolderYouAreHavingIssuesWith directory .

For any support related questions, you can also contact help@appdynamics.com.

## Custom Dashboad ##
![](https://raw.githubusercontent.com/Appdynamics/zookeeper-monitoring-extension/master/zookeeper.png)

## Contributing ##

Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/zookeeper-monitoring-extension).

## Version
|          Name            |  Version   |
|--------------------------|------------|
|Extension Version         |1.1.1       |
|Controller Compatibility  |4.5 or Later|
|Machine Agent Version     |4.5.13+     |
|Product Tested on         |3.4.14      |
|Last Update               |06/01/2021  |
