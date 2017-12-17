package com.emc.ocopea.dsb.h2;

import com.emc.microservice.Context;
import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceApplication;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.dsb.CopyServiceInstance;
import com.emc.ocopea.dsb.CopyServiceInstanceResponse;
import com.emc.ocopea.dsb.CreateServiceInstance;
import com.emc.ocopea.dsb.DsbInfo;
import com.emc.ocopea.dsb.DsbPlan;
import com.emc.ocopea.dsb.DsbSupportedCopyProtocol;
import com.emc.ocopea.dsb.DsbSupportedProtocol;
import com.emc.ocopea.dsb.DsbWebApi;
import com.emc.ocopea.dsb.ServiceInstance;
import com.emc.ocopea.dsb.ServiceInstanceDetails;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by liebea on 1/4/16.
 * Drink responsibly
 */
public class RemoteH2DsbResource implements DsbWebApi {

    private DsbInfo dsbInfo;
    private RemoteH2DsbSingleton remoteH2DsbSingleton;

    @NoJavadoc
    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        MicroService serviceDescriptor = context.getServiceDescriptor();
        dsbInfo = new DsbInfo(
                context.getMicroServiceBaseURI(),
                "datasource",
                serviceDescriptor.getDescription(),
                Collections.singletonList(
                        new DsbPlan(
                                "default",
                                "default",
                                "default",
                                "1$",
                                Arrays.asList(
                                        new DsbSupportedProtocol("postgres", null, null),
                                        new DsbSupportedProtocol("h2", null, null)
                                ),
                                Collections.singletonList(
                                        new DsbSupportedCopyProtocol("shpanRest", null)
                                ),
                                Collections.emptyMap())
                ));

        remoteH2DsbSingleton =
                context.getSingletonManager().getManagedResourceByName("h2-dsb-singleton").getInstance();

        //ShpanPaas
    }

    @Override
    public CopyServiceInstanceResponse copyServiceInstance(
            @PathParam("instanceId") String instanceId,
            CopyServiceInstance copyDetails) {
        remoteH2DsbSingleton.copy(instanceId, copyDetails);
        return new CopyServiceInstanceResponse(0, "yey", copyDetails.getCopyId());
    }

    @Override
    public ServiceInstance createServiceInstance(CreateServiceInstance serviceSettings) {
        if (serviceSettings.getRestoreInfo() != null) {
            remoteH2DsbSingleton.createFromCopy(serviceSettings.getRestoreInfo(), serviceSettings.getInstanceId());
        } else {
            remoteH2DsbSingleton.create(serviceSettings.getInstanceId());
        }
        return new ServiceInstance(serviceSettings.getInstanceId());
    }

    @Override
    public ServiceInstance deleteServiceInstance(@PathParam("instanceId") String instanceId) {
        remoteH2DsbSingleton.deleteDB(instanceId);
        return new ServiceInstance(instanceId);
    }

    @Override
    public Response getDSBIcon() {
        return Response.noContent().build();
    }

    @Override
    public DsbInfo getDSBInfo() {
        return dsbInfo;
    }

    @Override
    public ServiceInstanceDetails getServiceInstance(@PathParam("instanceId") String instanceId) {
        H2Instance h2Instance = remoteH2DsbSingleton.get(instanceId);
        if (h2Instance == null) {
            throw new NotFoundException("could not find service with id " + instanceId);
        }

        return remoteH2DsbSingleton.getH2EndpointInfo(h2Instance, false);
    }

    @Override
    public List<ServiceInstance> getServiceInstances() {
        return remoteH2DsbSingleton.list()
                .stream()
                .map(s -> new ServiceInstance(s.getName()))
                .collect(Collectors.toList());
    }

}
