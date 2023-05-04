package com.averygrimes.servicediscovery.registration;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.event.EventListener;
/**
 * @author Avery Grimes-Farrow
 * Created on: 10/9/19
 * https://github.com/helloavery
 */

public class ServiceDiscoveryListener{
    private final static String PORT_MATCHER = ".+:\\d+$";

    @EventListener
    public void handleEvent(final ServiceDiscoveryEvent discoveryEvent){
        String applicationURI = discoveryEvent.getServiceURI();
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