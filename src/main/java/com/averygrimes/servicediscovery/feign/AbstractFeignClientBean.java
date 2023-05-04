package com.averygrimes.servicediscovery.feign;

import com.netflix.client.config.IClientConfig;
import feign.Client;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import feign.form.spring.SpringFormEncoder;
import feign.jaxrs.JAXRSContract;
import feign.slf4j.Slf4jLogger;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.*;

import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;

/**
 * @author Avery Grimes-Farrow
 * Created on: 9/4/19
 * https://github.com/helloavery
 */

public abstract class AbstractFeignClientBean<T> implements FactoryBean<T>, InitializingBean, Client {

    private T client;
    private final Class<T> serviceInterface;

    protected AbstractFeignClientBean(Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    private ObjectFactory<HttpMessageConverters> messageConverters;

    private ObjectProvider<HttpMessageConverterCustomizer> customizers;

    @Inject
    public void setMessageConverters(ObjectFactory<HttpMessageConverters> messageConverters) {
        this.messageConverters = messageConverters;
    }

    @Inject
    public void setCustomizers(ObjectProvider<HttpMessageConverterCustomizer> customizers) {
        this.customizers = customizers;
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
        String serviceURL = resolveServiceURL();
        this.client = Feign.builder()
                .logger(new Slf4jLogger(serviceURL)).logLevel(Logger.Level.BASIC)
                .contract(new SpringMvcContract())
                .retryer(new Retryer.Default())
                .encoder(new SpringFormEncoder())
                .decoder(new ResponseEntityDecoder(new SpringDecoder(messageConverters, customizers)))
                .errorDecoder(new DiscoveryErrorDecoder<>())
                .client(this)
                .target(serviceInterface, serviceURL);
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        return new FeignResponseDelegate(null, null).execute(request, options);
    }

    protected abstract void customizeConfiguration(IClientConfig clientConfig);
    protected abstract String resolveServiceURL();
}