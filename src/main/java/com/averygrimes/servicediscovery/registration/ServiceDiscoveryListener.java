package com.averygrimes.servicediscovery.registration;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * @author Avery Grimes-Farrow
 * Created on: 10/9/19
 * https://github.com/helloavery
 */

public class ServiceDiscoveryListener implements ServletContextAware {

    private final Application application;
    private String applicationPath;
    private String applicationURI;
    private final static String PORT_MATCHER = ".+:\\d+$";

    protected ServiceDiscoveryListener(Application application) {
        this.application = application;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        applicationPath = servletContext.getContextPath() + resolveApplicationPath();
    }

    private String resolveApplicationPath(){
        try{
            return AnnotationUtils.findAnnotation(application.getClass(), ApplicationPath.class).value();
        }
        catch(NullPointerException e){
            return null;
        }
    }

    @EventListener
    public void handleEvent(final ServiceDiscoveryEvent discoveryEvent){
        applicationURI = discoveryEvent.getServiceURI() + applicationPath;
        String baseURI =  deriveBaseURI(discoveryEvent.getServiceURI());
        Integer port = derivePort(discoveryEvent.getServiceURI());
        onServiceDetailsPrepared(baseURI, port, applicationURI);
    }

    private static String deriveBaseURI(String URI){
        if(URI.matches(PORT_MATCHER)){
            String[] splitURI = URI.split(":");
            int spiltURILength = splitURI.length;
            StringBuilder stringBuilder = new StringBuilder("http:");
            for(int i = 1 ; i < spiltURILength - 1 ; i++){
                stringBuilder.append(splitURI[i]);
            }
            return stringBuilder.toString();
        }
        return URI;
    }

    private Integer derivePort(String URI){
        if(URI.matches(PORT_MATCHER)){
            String[] spiltURI = URI.split(":");
            int spiltURILength = spiltURI.length;
            return NumberUtils.toInt(spiltURI[spiltURILength - 1]);
        }
        return null;
    }

    protected void onServiceDetailsPrepared(String baseURI, Integer port, String applicationURI){}

}
