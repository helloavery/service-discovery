package com.averygrimes.servicediscovery.registration;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * @author Avery Grimes-Farrow
 * Created on: 10/11/19
 * https://github.com/helloavery
 */

@Component
public class ServiceDiscoveryPublisher {

    private Environment environment;
    private ApplicationEventPublisher applicationEventPublisher;

    private static final String ENVIRONMENT_PROPERTY = "environment";
    private static final String QA_HOST_PROPERTY = "qa.hosts";
    private static final String PROD_HOST_PROPERTY = "prod.hosts";

    @Inject
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Inject
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void publishEvent(){
        List<String> serviceHosts = getServiceHost();
        serviceHosts.forEach(host -> applicationEventPublisher.publishEvent(new ServiceDiscoveryEvent(this, host)));
    }

    @SuppressWarnings("unchecked")
    private List<String> getServiceHost(){
        if(environment.getProperty(ENVIRONMENT_PROPERTY).equalsIgnoreCase("QA")){
            return environment.getProperty(QA_HOST_PROPERTY, List.class);
        }
        else if(environment.getProperty(ENVIRONMENT_PROPERTY).equalsIgnoreCase("PROD")){
            return environment.getProperty(PROD_HOST_PROPERTY, List.class);
        }
        return Collections.emptyList();
    }

}
