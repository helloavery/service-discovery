package com.averygrimes.servicediscovery.feign;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.form.FormEncoder;
import feign.jackson.JacksonEncoder;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;


/**
 * @author Avery Grimes-Farrow
 * Created on: 10/7/19
 * https://github.com/helloavery
 */

public class DiscoveryFeignEncoder extends FormEncoder {

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        if (bodyType == String.class) {
            template.body(object.toString());
        } else if (bodyType == byte[].class) {
            template.body((byte[]) object, StandardCharsets.UTF_8);
        } else if (object != null) {
            JacksonEncoder delegate = new JacksonEncoder();
            delegate.encode(object, bodyType, template);
        }
    }
}
