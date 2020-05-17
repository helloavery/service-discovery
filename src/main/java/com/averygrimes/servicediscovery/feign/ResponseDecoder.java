package com.averygrimes.servicediscovery.feign;

import com.averygrimes.servicediscovery.jersey.FeignJaxrsResponse;
import com.fasterxml.jackson.databind.Module;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.jackson.JacksonDecoder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * @author Avery Grimes-Farrow
 * Created on: 9/30/19
 * https://github.com/helloavery
 */

public class ResponseDecoder<T> extends JacksonDecoder {

    private Class<T> serviceInterface;

    public ResponseDecoder(Class<T> serviceInterface) {
        this(new ArrayList<>(), serviceInterface);
    }

    public ResponseDecoder(Iterable<Module> modules, Class<T> serviceInterface) {
        super(modules);
        this.serviceInterface = serviceInterface;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if(javax.ws.rs.core.Response.class.equals(type)){
            return new FeignJaxrsResponse<>(response, serviceInterface);
        }
        else if(String.class.equals(type)){
            return decodeAsString(response, type);
        }
        else{
            return super.decode(response, type);
        }
    }

    private Object decodeAsString(Response response, Type type) throws IOException{
        Response.Body body =  response.body();
        if(body == null){
            return null;
        }
        if(String.class.equals(type)){
            return Util.toString(body.asReader());
        }
        throw new DecodeException(500, type + " is not a type supported by this decoder", response.request());
    }
}
