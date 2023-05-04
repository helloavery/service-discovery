package com.averygrimes.servicediscovery;

import com.averygrimes.servicediscovery.config.ServiceDiscoveryConfig;
import com.averygrimes.servicediscovery.interaction.ClientRestConfig;
import com.averygrimes.servicediscovery.registration.ServiceDiscoveryPublisher;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Avery Grimes-Farrow
 * Created on: 10/11/19
 * https://github.com/helloavery
 */

@Import({ServiceDiscoveryConfig.class, ClientRestConfig.class, ServiceDiscoveryPublisher.class})
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
public @interface EnableServiceDiscovery {
}