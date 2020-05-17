package com.averygrimes.servicediscovery.feign;

import com.netflix.client.config.IClientConfig;
import feign.Client;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Retryer;
import feign.jaxrs.JAXRSContract;
import feign.slf4j.Slf4jLogger;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;

/**
 * @author Avery Grimes-Farrow
 * Created on: 9/4/19
 * https://github.com/helloavery
 */

public abstract class AbstractFeignClientBean<T> implements FactoryBean<T>, InitializingBean, Client {

    private T client;
    private Class<T> serviceInterface;
    private String serviceURL;

    protected AbstractFeignClientBean(Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    @Override
    public T getObject() throws Exception {
        return this.client;
    }

    @Override
    public Class<?> getObjectType() {
        return serviceInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        serviceURL = resolveServiceURL();
        setupClient();
        this.client = Feign.builder().logger(new Slf4jLogger(serviceURL)).logLevel(Logger.Level.BASIC)
                .contract(new JAXRSContract())
                .retryer(new Retryer.Default())
                .encoder(new DiscoveryFeignEncoder())
                .decoder(new ResponseDecoder<>(serviceInterface))
                .errorDecoder(new DiscoveryErrorDecoder<>())
                .client(this)
                .target(serviceInterface, serviceURL);
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        return new FeignResponseDelegate(null, null).execute(request, options);
    }


    private void setupClient(){
        javax.ws.rs.client.Client jerseyClient = ClientBuilder.newClient();
        jerseyClient.property(ClientProperties.CONNECT_TIMEOUT, "");
        jerseyClient.property(ClientProperties.READ_TIMEOUT, "");
    }

    protected abstract void customizeConfiguration(IClientConfig clientConfig);
    protected abstract String resolveServiceURL();
}
