package com.averygrimes.servicediscovery.consul;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * @author Avery Grimes-Farrow
 * Created on: 10/9/19
 * https://github.com/helloavery
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsulServiceInfo {

    private String ID;
    private String Service;
    private String Name;
    private String Address;
    private Map<String, String> Meta;
    private Integer Port;
    private ConsulServiceCheck Check;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getService() {
        return Service;
    }

    public void setService(String service) {
        Service = service;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public Map<String, String> getMeta() {
        return Meta;
    }

    public void setMeta(Map<String, String> meta) {
        Meta = meta;
    }

    public Integer getPort() {
        return Port;
    }

    public void setPort(Integer port) {
        Port = port;
    }

    public ConsulServiceCheck getCheck() {
        return Check;
    }

    public void setCheck(ConsulServiceCheck check) {
        Check = check;
    }
}
