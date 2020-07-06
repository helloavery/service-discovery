package com.averygrimes.servicediscovery.interaction;

import com.averygrimes.servicediscovery.consul.ConsulServiceCheck;
import com.averygrimes.servicediscovery.consul.ConsulServiceInfo;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Avery Grimes-Farrow
 * Created on: 10/9/19
 * https://github.com/helloavery
 */

@Named
public interface ConsulDiscoveryClient {

    @Path("/health/service/{serviceId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response retrieveService(@PathParam("serviceId") String serviceId, @QueryParam("status") String status, @QueryParam("filter") String filter);

    @Path("/agent/service/register")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response publishService(ConsulServiceInfo consulServiceInfo);

    @Path("/agent/check/register")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response registerCheck(ConsulServiceCheck consulServiceCheck);
}
