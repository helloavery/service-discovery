package com.averygrimes.servicediscovery;

import com.averygrimes.servicediscovery.consul.ConsulServiceInfo;
import com.averygrimes.servicediscovery.consul.ConsulServiceResponse;
import com.averygrimes.servicediscovery.feign.AbstractFeignClientBean;
import com.averygrimes.servicediscovery.interaction.ConsulDiscoveryClient;
import com.netflix.client.config.IClientConfig;
import org.apache.commons.collections4.CollectionUtils;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author Avery Grimes-Farrow
 * Created on: 9/4/19
 * https://github.com/helloavery
 */

public class RestFeignClientBean<T> extends AbstractFeignClientBean {

    private ConsulDiscoveryClient consulDiscoveryClient;

    private String service;
    private String version;
    private String instance;

    public RestFeignClientBean(Class<T> serviceInterface, String service, String version, String instance) {
        super(serviceInterface);
        this.service = service;
        this.version = version;
        this.instance = instance;
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
        Response consulServiceResponse = consulDiscoveryClient.retrieveService(service);
        validateResponse(consulServiceResponse);
        List<ConsulServiceResponse> serviceInfoList  = consulServiceResponse.readEntity(new GenericType<List<ConsulServiceResponse>>(){});

        List<ConsulServiceInfo> filteredServiceInfoList = serviceInfoList.stream().filter(res -> res.getService().getMeta().get("version").equals(VersioningUtils.deriveVersion(version)))
                .map(ConsulServiceResponse::getService).collect(Collectors.toList());

        //Get random element. Consul internally load balances but we want to randomly retrieve an item just in case more than one is returned
        Random rand = new Random();
        ConsulServiceInfo filteredServiceInfo = filteredServiceInfoList.get(rand.nextInt(filteredServiceInfoList.size()));
        return filteredServiceInfo.getMeta().get("applicationPath");
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
