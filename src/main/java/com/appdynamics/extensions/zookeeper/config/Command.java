package com.appdynamics.extensions.zookeeper.config;

import java.util.HashSet;
import java.util.Set;

public class Command {

    private String command;
    private String separator;
    Set<String> fields = new HashSet<String>();

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

    public Set<String> getFields() {
        return fields;
    }

    public void setFields(Set<String> fields) {
        this.fields = fields;
    }
}
