package com.averygrimes.servicediscovery;

import com.averygrimes.servicediscovery.feign.AbstractFeignClientBean;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;

/**
 * @author Avery Grimes-Farrow
 * Created on: 10/1/19
 * https://github.com/helloavery
 */

public class SimpleFeignClientBean<T> extends AbstractFeignClientBean<T> {

    private String URL;

    public SimpleFeignClientBean(Class<T> serviceInterface, String URL) {
        super(serviceInterface);
        this.URL = URL;
    }

    @Override
    protected void customizeConfiguration(IClientConfig clientConfig){
        clientConfig.set(CommonClientConfigKey.ListOfServers, URL);
    }

    @Override
    protected String resolveServiceURL() {
        return this.URL;
    }
}