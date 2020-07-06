package com.averygrimes.servicediscovery;

import com.averygrimes.servicediscovery.consul.ConsulServiceInfo;
import com.averygrimes.servicediscovery.consul.ConsulServiceResponse;
import com.averygrimes.servicediscovery.feign.AbstractFeignClientBean;
import com.averygrimes.servicediscovery.interaction.ConsulDiscoveryClient;
import com.netflix.client.config.IClientConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Avery Grimes-Farrow
 * Created on: 9/4/19
 * https://github.com/helloavery
 */

public class RestFeignClientBean<T> extends AbstractFeignClientBean<T> {

    private ConsulDiscoveryClient consulDiscoveryClient;

    private final String service;
    private final String version;

    private final AtomicInteger position = new AtomicInteger(0);

    public RestFeignClientBean(Class<T> serviceInterface, String service, String version) {
        super(serviceInterface);
        this.service = service;
        this.version = version;
    }

    @Inject
    public void setConsulDiscoveryClient(ConsulDiscoveryClient consulDiscoveryClient) {
        this.consulDiscoveryClient = consulDiscoveryClient;
    }

    @Override
    protected void customizeConfiguration(IClientConfig clientConfig) {

    }

    @Override
    protected String resolveServiceURL() {
        String serviceURL = null;
        String derivedVersion = VersioningUtils.deriveVersion(version);
        Response consulServiceResponse = consulDiscoveryClient.retrieveService(service, "passing", "Service.Meta.version=="+derivedVersion);
        validateResponse(consulServiceResponse);
        List<ConsulServiceResponse> serviceInfoList  = consulServiceResponse.readEntity(new GenericType<List<ConsulServiceResponse>>(){});

        List<ConsulServiceInfo> filteredServiceInfoList = serviceInfoList.stream().filter(res -> res.getService().getMeta().get("version").equals(VersioningUtils.deriveVersion(version)))
                .map(ConsulServiceResponse::getService).collect(Collectors.toList());

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

    private void validateResponse(Response response){
        if(response == null){
            throw new WebApplicationException("Load balancer provided no available servers for " + service + "-" + version);
        }
        else if(CollectionUtils.isEmpty(response.readEntity(new GenericType<List<ConsulServiceResponse>>(){}))){
            throw new WebApplicationException("Load balancer provided no available servers for " + service + "-" + version, response);
        }
    }

}
