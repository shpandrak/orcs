package com.emc.ocopea.demo.dsb.shpanblob;

import com.emc.microservice.Context;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.blobstore.DuplicateObjectKeyException;
import com.emc.microservice.blobstore.IllegalStoreStateException;
import com.emc.microservice.blobstore.ObjectKeyFormatException;
import com.emc.microservice.blobstore.impl.TempFileSystemBlobStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by liebea on 6/26/16.
 * Drink responsibly
 */
public class RemoteShpanBlobResource implements ShpanBlobRemoteWebAPI {
    private static final Logger log = LoggerFactory.getLogger(RemoteShpanBlobDSBSingleton.class);
    private RemoteShpanBlobDSBSingleton remoteShpanBlobDSBSingleton;

    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        remoteShpanBlobDSBSingleton =
                context.getSingletonManager().getManagedResourceByName("remote-shpanblob-dsb-singleton").getInstance();
    }

    @Override
    public Response createBlob(
            @PathParam("serviceId") String serviceId,
            @PathParam("namespace") String namespace,
            @PathParam("key") String key,
            InputStream body) {
        try {
            //todo:parse headers
            Map<String, String> parameters = new HashMap<>();
            getStore(serviceId).create(namespace, key, parameters, body);
        } catch (ObjectKeyFormatException e) {
            log.debug("Illegal key or namespace format.", e);
            return Response.status(Response.Status.BAD_REQUEST).entity("Illegal namespace or key format.").build();
        } catch (DuplicateObjectKeyException e) {
            log.error("Object with the namespace : '{}' and key : '{}' already exists.", namespace, key, e);
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity("Object with the same namespace and key already exists.")
                    .build();
        } catch (Exception e) {
            log.error("System error whilst creating object with namespace {} and key {}.", namespace, key, e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("System error whilst creating object.")
                    .build();
        }
        return Response.ok().build();
    }

    @Override
    public Response updateBlob(
            @PathParam("serviceId") String serviceId,
            @PathParam("namespace") String namespace,
            @PathParam("key") String key,
            InputStream body) {
        try {
            //todo:parse headers
            Map<String, String> parameters = new HashMap<>();
            getStore(serviceId).update(namespace, key, parameters, body);
        } catch (ObjectKeyFormatException e) {
            log.debug("Illegal key or namespace format.", e);
            return Response.status(Response.Status.BAD_REQUEST).entity("Illegal namespace or key format.").build();
        } catch (IllegalStoreStateException e) {
            log.error("System error whilst creating object with namespace {} and key {}.", namespace, key, e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("System error whilst creating object.")
                    .build();
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }

        return Response.ok().build();
    }

    @Override
    public Response readBlob(
            @PathParam("serviceId") String serviceId,
            @PathParam("namespace") String namespace,
            @PathParam("key") String key) {
        StreamingOutput so = output -> {
            try {
                getStore(serviceId).readBlob(namespace, key, output);
            } catch (ObjectKeyFormatException e) {
                throw new NotFoundException("Illegal key or namespace format.", e);
            } catch (IllegalStoreStateException e) {
                log.error("System error whilst creating object with namespace {} and key {}.", namespace, key, e);
                throw new WebApplicationException(e);
            } catch (WebApplicationException wae) {
                throw wae;
            } catch (Exception e) {
                throw new WebApplicationException(e);
            }

        };
        return Response.ok(so).build();
    }

    @Override
    public Response delete(
            @PathParam("serviceId") String serviceId,
            @PathParam("namespace") String namespace,
            @PathParam("key") String key) {
        try {
            getStore(serviceId).delete(namespace, key);
        } catch (ObjectKeyFormatException e) {
            throw new BadRequestException(e);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
        return Response.ok().build();
    }

    @Override
    public Response isExists(
            @PathParam("serviceId") String serviceId,
            @PathParam("namespace") String namespace,
            @PathParam("key") String key) {
        try {
            if (getStore(serviceId).isExists(namespace, key)) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.GONE).build();
            }
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @Override
    public Collection<ShpanBlobKeyDTO> list(@PathParam("serviceId") String serviceId) {
        try {
            return getStore(serviceId).list()
                    .stream()
                    .map(b -> new ShpanBlobKeyDTO(b.getNamespace(), b.getKey()))
                    .collect(Collectors.toList());
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    private TempFileSystemBlobStore getStore(String serviceId) {
        RemoteShpanBlobInstance remoteShpanBlobInstance = remoteShpanBlobDSBSingleton.get(serviceId);
        if (remoteShpanBlobInstance == null) {
            throw new NotFoundException("serviceId not found " + serviceId);
        }
        return remoteShpanBlobInstance.getStore();
    }
}
