package com.averygrimes.servicediscovery.consul;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Avery Grimes-Farrow
 * Created on: 10/14/19
 * https://github.com/helloavery
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsulServiceResponse {

    private ConsulServiceInfo Service;

    public ConsulServiceInfo getService() {
        return Service;
    }

    public void setService(ConsulServiceInfo service) {
        Service = service;
    }
}
