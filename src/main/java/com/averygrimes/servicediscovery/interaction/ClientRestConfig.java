package com.averygrimes.servicediscovery.interaction;

import com.averygrimes.servicediscovery.SimpleFeignClientBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import javax.inject.Inject;

/**
 * @author Avery Grimes-Farrow
 * Created on: 9/30/19
 * https://github.com/helloavery
 */

@Configuration("ServiceDiscoveryClientRestConfig")
public class ClientRestConfig {

    private Environment environment;
    private static final String CONSUL_PROPERTY = "consul.environment";
    private static final String QA_CONSUL_URI = "http://localhost:8500/v1";
    private static final String PROD_CONSUL_URI = "http://localhost:8500/v1";

    @Inject
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public SimpleFeignClientBean<ConsulDiscoveryClient> createConsulClient(){
        String consulURI = getConsulURI();
        Assert.notNull(consulURI, "Could not establish Consul URI, Consul URI came back null");
        return new SimpleFeignClientBean<>(ConsulDiscoveryClient.class, consulURI);
    }

    private String getConsulURI(){
        if(environment.getProperty(CONSUL_PROPERTY).equalsIgnoreCase("QA")){
            return QA_CONSUL_URI;
        }
        else if(environment.getProperty(CONSUL_PROPERTY).equalsIgnoreCase("PROD")){
            return PROD_CONSUL_URI;
        }
        return null;
    }

}
