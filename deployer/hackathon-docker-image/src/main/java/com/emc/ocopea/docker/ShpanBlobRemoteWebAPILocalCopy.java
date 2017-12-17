package com.emc.ocopea.docker;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Collection;

/**
 * Created by liebea on 6/26/16.
 * Drink responsibly
 */
@Path("blob")
public interface ShpanBlobRemoteWebAPILocalCopy {

    @POST
    @Path("{serviceId}/{namespace}/{key}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    Response createBlob(
            @PathParam("serviceId") String serviceId,
            @PathParam("namespace") String namespace,
            @PathParam("key") String key,
            InputStream body);

    @PUT
    @Path("{serviceId}/{namespace}/{key}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateBlob(
            @PathParam("serviceId") String serviceId,
            @PathParam("namespace") String namespace,
            @PathParam("key") String key,
            InputStream body);

    @GET
    @Path("{serviceId}/{namespace}/{key}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response readBlob(
            @PathParam("serviceId") String serviceId,
            @PathParam("namespace") final String namespace,
            @PathParam("key") final String key);

    @DELETE
    @Path("{serviceId}/{namespace}/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    Response delete(
            @PathParam("serviceId") String serviceId,
            @PathParam("namespace") String namespace,
            @PathParam("key") String key);

    @HEAD
    @Path("{serviceId}/{namespace}/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    Response isExists(
            @PathParam("serviceId") String serviceId,
            @PathParam("namespace") String namespace,
            @PathParam("key") String key);

    @GET
    @Path("{serviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    Collection<ShpanBlobKeyDTOLocalCopy> list(@PathParam("serviceId") String serviceId);

}
