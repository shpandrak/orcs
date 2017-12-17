package com.emc.ocopea.demo.dsb.shpanblob;

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

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RemoteShpanBlobDSBResource implements DsbWebApi {

    private DsbInfo dsbInfo;
    private RemoteShpanBlobDSBSingleton remoteShpanBlobDSBSingleton;

    @NoJavadoc
    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        MicroService serviceDescriptor = context.getServiceDescriptor();
        dsbInfo = new DsbInfo(
                context.getMicroServiceBaseURI(),
                "blobstore",
                serviceDescriptor.getDescription(),

                Collections.singletonList(
                        new DsbPlan(
                                "default",
                                "default",
                                "default",
                                "1$",
                                Arrays.asList(
                                        new DsbSupportedProtocol("s3", null, null),
                                        new DsbSupportedProtocol("shpanblob", null, null)
                                ),
                                Collections.singletonList(
                                        new DsbSupportedCopyProtocol("shpanRest", null)
                                ),

                                Collections.emptyMap())));
        remoteShpanBlobDSBSingleton =
                context.getSingletonManager().getManagedResourceByName("remote-shpanblob-dsb-singleton").getInstance();
    }

    @Override
    public CopyServiceInstanceResponse copyServiceInstance(
            @PathParam("instanceId") String instanceId,
            CopyServiceInstance copyDetails) {
        remoteShpanBlobDSBSingleton.copy(instanceId, copyDetails);
        return new CopyServiceInstanceResponse(0, "yey", copyDetails.getCopyId());
    }

    @Override
    public ServiceInstance createServiceInstance(CreateServiceInstance serviceSettings) {

        if (serviceSettings.getRestoreInfo() == null) {
            remoteShpanBlobDSBSingleton.create(serviceSettings.getInstanceId());
        } else {
            remoteShpanBlobDSBSingleton.createFromCopy(
                    serviceSettings.getInstanceId(),
                    serviceSettings.getRestoreInfo());
        }
        return new ServiceInstance(serviceSettings.getInstanceId());
    }

    @Override
    public ServiceInstance deleteServiceInstance(@PathParam("instanceId") String instanceId) {
        remoteShpanBlobDSBSingleton.delete(instanceId);
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
        return remoteShpanBlobDSBSingleton.getEndpointInfo(instanceId);
    }

    @Override
    public List<ServiceInstance> getServiceInstances() {
        return remoteShpanBlobDSBSingleton.list()
                .stream()
                .map(s -> new ServiceInstance(s.getName()))
                .collect(Collectors.toList());
    }
}
