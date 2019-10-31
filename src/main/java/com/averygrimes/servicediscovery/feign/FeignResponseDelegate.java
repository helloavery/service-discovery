package com.averygrimes.servicediscovery.feign;

import feign.Client;
import feign.Request;
import feign.Response;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Avery Grimes-Farrow
 * Created on: 9/30/19
 * https://github.com/helloavery
 */

public class FeignResponseDelegate extends Client.Default {

    protected FeignResponseDelegate(SSLSocketFactory sslContextFactory, HostnameVerifier hostnameVerifier) {
        super(sslContextFactory, hostnameVerifier);
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        Response response = super.execute(request, options);

        InputStream responseBodyInputStream = response.body().asInputStream();
        byte[] bytes = IOUtils.toByteArray(responseBodyInputStream);
        return response.toBuilder()
                .body(bytes)
                .build();
    }
}
