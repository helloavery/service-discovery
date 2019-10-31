package com.averygrimes.servicediscovery.consul;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Avery Grimes-Farrow
 * Created on: 10/13/19
 * https://github.com/helloavery
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsulServiceCheck {

    private String Name;
    private String HTTP;
    private String Interval;
    private String ServiceID;
    private boolean tls_skip_verify;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getInterval() {
        return Interval;
    }

    public void setInterval(String interval) {
        Interval = interval;
    }

    public String getHTTP() {
        return HTTP;
    }

    public void setHTTP(String HTTP) {
        this.HTTP = HTTP;
    }

    public String getServiceID() {
        return ServiceID;
    }

    public void setServiceID(String serviceID) {
        ServiceID = serviceID;
    }

    public boolean getTls_skip_verify() {
        return tls_skip_verify;
    }

    public void setTls_skip_verify(boolean tls_skip_verify) {
        this.tls_skip_verify = tls_skip_verify;
    }
}
