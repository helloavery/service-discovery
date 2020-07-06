package com.averygrimes.servicediscovery.registration;

import com.averygrimes.servicediscovery.EnableServiceDiscovery;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Avery Grimes-Farrow
 * Created on: 10/9/19
 * https://github.com/helloavery
 */

@Component
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
@EnableServiceDiscovery
public @interface ServiceDiscoveryRegister {

    String service();

    String version();

    String healthCheckPath();
}
