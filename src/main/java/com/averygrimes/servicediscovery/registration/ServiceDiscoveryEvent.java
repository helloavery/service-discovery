package com.averygrimes.servicediscovery.registration;

import org.springframework.context.ApplicationEvent;

/**
 * @author Avery Grimes-Farrow
 * Created on: 10/10/19
 * https://github.com/helloavery
 */

public class ServiceDiscoveryEvent extends ApplicationEvent {

    private final String serviceURI;

    public ServiceDiscoveryEvent(Object source, String serviceURI) {
        super(source);
        this.serviceURI = serviceURI;
    }

    public String getServiceURI() {
        return serviceURI;
    }
}