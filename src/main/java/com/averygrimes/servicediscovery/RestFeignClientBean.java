package com.averygrimes.servicediscovery;

import com.averygrimes.servicediscovery.exception.ServiceDiscoveryException;
import com.averygrimes.servicediscovery.feign.AbstractFeignClientBean;
import com.averygrimes.servicediscovery.model.ConsulServiceArrayResponse;
import com.averygrimes.servicediscovery.model.ConsulServiceInfo;
import com.averygrimes.servicediscovery.model.ConsulServiceResponse;
import com.averygrimes.servicediscovery.utils.VersioningUtils;
import com.netflix.client.config.IClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Avery Grimes-Farrow
 * Created on: 9/4/19
 * https://github.com/helloavery
 */

@Slf4j
public class RestFeignClientBean<T> extends AbstractFeignClientBean<T> {

    private ConsulDiscoveryClientApi consulDiscoveryClient;

    private final String service;
    private final String version;

    private final AtomicInteger position = new AtomicInteger(0);

    public RestFeignClientBean(Class<T> serviceInterface, String service, String version) {
        super(serviceInterface);
        this.service = service;
        this.version = version;
    }

    @Inject
    public void setConsulDiscoveryClient(ConsulDiscoveryClientApi consulDiscoveryClient) {
        this.consulDiscoveryClient = consulDiscoveryClient;
    }

    @Override
    protected void customizeConfiguration(IClientConfig clientConfig) {
        //do nothing; this method will not do anything here
    }

    @Override
    protected String resolveServiceURL() {
        String serviceURL = null;
        String derivedVersion = VersioningUtils.deriveVersion(version);
        ResponseEntity<ConsulServiceArrayResponse> consulServiceResponseArray = consulDiscoveryClient.retrieveService(service, "passing", "Service.Meta.version=="+derivedVersion);
        List<ConsulServiceResponse> serviceInfoList  = validateResponseAndReturnServiceList(consulServiceResponseArray);
        List<ConsulServiceInfo> filteredServiceInfoList = serviceInfoList.stream().map(ConsulServiceResponse::getService)
                .filter(resService -> resService.getMeta().get("version").equals(VersioningUtils.deriveVersion(version))).collect(Collectors.toList());

        // Round Robin algorithm to load balance hosts. We are returned multiple passing hosts from Consul
        if (position.get() > filteredServiceInfoList.size() - 1) {
            position.set(0);
        }
        ConsulServiceInfo filteredServiceInfo = filteredServiceInfoList.get(position.get());
        if(StringUtils.isNotBlank(filteredServiceInfo.getMeta().get("applicationPath"))){
            serviceURL = filteredServiceInfo.getMeta().get("applicationPath");
        }
        position.getAndIncrement();

        if(serviceURL == null){
            throw new WebApplicationException("Load balancer provided no available servers for " + service + "-" + version);
        }
        return serviceURL;
    }

    private List<ConsulServiceResponse>  validateResponseAndReturnServiceList(ResponseEntity<ConsulServiceArrayResponse> consulServiceArrayResponse){
        if(consulServiceArrayResponse == null || consulServiceArrayResponse.getStatusCodeValue() < 200 || consulServiceArrayResponse.getStatusCodeValue() > 299){
            throw new ServiceDiscoveryException("Error while calling Load Balance service, either returned null response or invalid response code for " + service + "-" + version);
        }else if(CollectionUtils.isEmpty(consulServiceArrayResponse.getBody())){
            throw new ServiceDiscoveryException("Load balancer provided no available servers for " + service + "-" + version);
        }
        log.info("Successfully fetched available services from Load Balancer for " + service + "-" + version);
        return consulServiceArrayResponse.getBody();
    }
}