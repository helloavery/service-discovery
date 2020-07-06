package com.averygrimes.servicediscovery.registration;

import com.averygrimes.servicediscovery.VersioningUtils;
import com.averygrimes.servicediscovery.consul.ConsulServiceCheck;
import com.averygrimes.servicediscovery.consul.ConsulServiceInfo;
import com.averygrimes.servicediscovery.interaction.ConsulDiscoveryClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

import javax.inject.Inject;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Avery Grimes-Farrow
 * Created on: 10/9/19
 * https://github.com/helloavery
 */

public class ServiceDiscoveryRegistrationProcessor extends ServiceDiscoveryListener implements BeanFactoryAware {

    private static final Logger LOGGER = LogManager.getLogger(ServiceDiscoveryRegistrationProcessor.class);
    private ListableBeanFactory beanFactory;
    private ConsulDiscoveryClient consulDiscoveryClient;

    public ServiceDiscoveryRegistrationProcessor(Application application) {
        super(application);
    }

    @Inject
    public void setConsulDiscoveryClient(ConsulDiscoveryClient consulDiscoveryClient) {
        this.consulDiscoveryClient = consulDiscoveryClient;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = (ListableBeanFactory) beanFactory;
    }

    @Override
    protected void onServiceDetailsPrepared(String baseURI, Integer port, String applicationURI){
        beanFactory.getBeansWithAnnotation(ServiceDiscoveryRegister.class).values().stream()
                .map(bean -> AnnotationUtils.findAnnotation(bean.getClass(), ServiceDiscoveryRegister.class))
                .map(service -> {
                    ConsulServiceInfo consulServiceInfo = new ConsulServiceInfo();
                    consulServiceInfo.setName(service.service());
                    consulServiceInfo.setAddress(baseURI);
                    consulServiceInfo.setPort(port);

                    Map<String, String> meta = new HashMap<>();
                    String version = VersioningUtils.deriveVersion(service.version());
                    meta.put("version", version);
                    meta.put("applicationPath", applicationURI);
                    consulServiceInfo.setMeta(meta);

                    ConsulServiceCheck consulServiceCheck = new ConsulServiceCheck();
                    consulServiceCheck.setName(service.service() + "-healthCheck");
                    StringBuilder healthCheckPath = new StringBuilder(baseURI);
                    if(port != null){
                        healthCheckPath.append(":").append(port);
                    }
                    healthCheckPath.append(service.healthCheckPath());
                    consulServiceCheck.setHTTP(healthCheckPath.toString());
                    consulServiceCheck.setInterval("10s");
                    consulServiceCheck.setServiceID(service.service());
                    consulServiceCheck.setTls_skip_verify(true);
                    consulServiceInfo.setCheck(consulServiceCheck);

                    return consulServiceInfo;
                }).forEach(this::publishService);
    }

    private void publishService(ConsulServiceInfo consulServiceInfo){
        Response consulResponse  = consulDiscoveryClient.publishService(consulServiceInfo);
        if(consulResponse.getStatus() > 299){
            LOGGER.error("Error registering service {}, error is {}", consulServiceInfo.getName(), consulResponse);
        }
        else{
            LOGGER.info("Consul registration of service {} successful", consulServiceInfo.getName());
        }
    }
}
