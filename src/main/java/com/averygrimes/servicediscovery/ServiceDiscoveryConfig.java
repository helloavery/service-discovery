package com.averygrimes.servicediscovery;

import com.averygrimes.servicediscovery.registration.ServiceDiscoveryRegistrationProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.core.Application;

/**
 * @author Avery Grimes-Farrow
 * Created on: 10/11/19
 * https://github.com/helloavery
 */

@Configuration
public class ServiceDiscoveryConfig {

    @Bean
    public ServiceDiscoveryRegistrationProcessor discoveryRegistrationProcessor(Application application) {
        return new ServiceDiscoveryRegistrationProcessor(application);
    }
}
