package com.averygrimes.servicediscovery.registration;

import com.averygrimes.servicediscovery.ConsulDiscoveryClientApi;
import com.averygrimes.servicediscovery.utils.VersioningUtils;
import com.averygrimes.servicediscovery.model.ConsulServiceCheck;
import com.averygrimes.servicediscovery.model.ConsulServiceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Avery Grimes-Farrow
 * Created on: 10/9/19
 * https://github.com/helloavery
 */

@Slf4j
@Component
public class ServiceDiscoveryRegistrationProcessor extends ServiceDiscoveryListener{

    private Environment environment;
    private ConsulDiscoveryClientApi consulDiscoveryClient;

    @Autowired
    public void setEnvironment(Environment environment){
        this.environment = environment;
    }

    @Autowired
    public void setConsulDiscoveryClient(ConsulDiscoveryClientApi consulDiscoveryClient) {
        this.consulDiscoveryClient = consulDiscoveryClient;
    }

    @Override
    protected void onServiceDetailsPrepared(String baseURI, Integer port, String applicationURI){
        if(StringUtils.isAnyBlank(environment.getProperty("servicediscovery.register.service"),
                environment.getProperty("servicediscovery.register.version"),
                environment.getProperty("servicediscovery.register.healthcheck"))){
            log.info("Skipping Service Discovery Registry, missing one or more required Service Discovery properties");
        }else{
            String serviceName = environment.getProperty("servicediscovery.register.service");
            String version = VersioningUtils.deriveVersion(environment.getProperty("servicediscovery.register.version"));
            String healthCheckPath = environment.getProperty("servicediscovery.register.healthcheck");

            ConsulServiceInfo consulServiceInfo = new ConsulServiceInfo();
            consulServiceInfo.setName(serviceName);
            consulServiceInfo.setAddress(baseURI);
            consulServiceInfo.setPort(port);

            Map<String, String> meta = new HashMap<>();
            meta.put("version", version);
            meta.put("applicationPath", applicationURI);
            consulServiceInfo.setMeta(meta);

            ConsulServiceCheck consulServiceCheck = new ConsulServiceCheck();
            consulServiceCheck.setName(serviceName + "-healthCheck");

            StringBuilder healthCheckPathStringBuilder = new StringBuilder(baseURI);
            if(port != null){
                healthCheckPathStringBuilder.append(":").append(port);
            }
            healthCheckPathStringBuilder.append(healthCheckPath);
            consulServiceCheck.setHTTP(healthCheckPathStringBuilder.toString());

            consulServiceCheck.setInterval("10s");
            consulServiceCheck.setServiceID(serviceName);
            consulServiceCheck.setTlsSkipVerify(true);
            consulServiceInfo.setCheck(consulServiceCheck);
            publishService(consulServiceInfo);

        }
    }

    private void publishService(ConsulServiceInfo consulServiceInfo){
        ResponseEntity<Void> consulResponse  = consulDiscoveryClient.publishService(consulServiceInfo);
        if(consulResponse.getStatusCodeValue() > 299){
            log.error("Error registering service {}, error is {}", consulServiceInfo.getName(), consulResponse);
        }
        else{
            log.info("Consul registration of service {} successful", consulServiceInfo.getName());
        }
    }
}