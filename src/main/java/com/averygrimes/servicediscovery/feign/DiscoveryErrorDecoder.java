package com.averygrimes.servicediscovery.feign;

import com.averygrimes.servicediscovery.exception.ServiceDiscoveryException;
import feign.Response;
import feign.codec.ErrorDecoder;


/**
 * @author Avery Grimes-Farrow
 * Created on: 9/30/19
 * https://github.com/helloavery
 */

public class DiscoveryErrorDecoder<T> extends ErrorDecoder.Default {

    @Override
    public Exception decode(String methodKey, Response response) {
        return new ServiceDiscoveryException(response.reason());
    }
}
