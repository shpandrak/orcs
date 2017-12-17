package com.emc.ocopea.demo.dsb.shpanblob;

import com.emc.microservice.Context;
import com.emc.microservice.blobstore.BlobStore;
import com.emc.microservice.blobstore.impl.TempFileSystemBlobStore;
import com.emc.microservice.discovery.DiscoveredService;
import com.emc.microservice.discovery.ServiceDiscoveryManager;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.crb.CrbWebDataApi;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.dsb.BindingPort;
import com.emc.ocopea.dsb.CopyServiceInstance;
import com.emc.ocopea.dsb.DsbRestoreCopyInfo;
import com.emc.ocopea.dsb.ServiceInstanceDetails;
import org.apache.commons.io.input.CountingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RemoteShpanBlobDSBSingleton implements ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(RemoteShpanBlobDSBSingleton.class);
    private static final long EMPTY_BLOBSTORE_SIZE = 50L;
    private final Map<String, RemoteShpanBlobInstance> serviceInstancesMap = new HashMap<>();
    private String ownServiceURN;
    private ServiceDiscoveryManager serviceDiscoveryManager;
    private WebAPIResolver webAPIResolver;

    @Override
    public void init(Context context) {
        log.info("Initialized shpanblob Singleton");
        ownServiceURN = context.getMicroServiceBaseURI();
        //very platform specific, but it is what it is...
        // should be part of ms lib
        serviceDiscoveryManager = context.getServiceDiscoveryManager();
        webAPIResolver = context.getWebAPIResolver();
    }

    @Override
    public void shutDown() {
        log.info("Shutting down shpanblob singleton");

    }

    public void create(String name) {
        create(name, false);
    }

    public void create(final String name, boolean readonly) {
        checkExists(name);
        createDB(name, readonly);
    }

    public boolean exists(String name) {
        return serviceInstancesMap.containsKey(name);
    }

    private void checkExists(String name) {
        if (exists(name)) {
            throw new IllegalArgumentException("ShpanBlob instance by name " + name + " already exists");
        }
    }

    private void createDB(final String name, boolean readonly) {

        RemoteShpanBlobInstance instance = new RemoteShpanBlobInstance(
                name,
                null,
                new Date(),
                readonly,
                new TempFileSystemBlobStore());
        instance.setSize(EMPTY_BLOBSTORE_SIZE);
        add(instance);
    }

    private void createDB(final String name, final InputStream copyStream, boolean readonly) {

        RemoteShpanBlobInstance instance = new RemoteShpanBlobInstance(name, null, new Date(), readonly,
                new TempFileSystemBlobStore());
        add(instance);

        final BlobStore blobStoreAPI = instance.getStore();
        long size = EMPTY_BLOBSTORE_SIZE;

        // Building from copy
        if (copyStream != null) {

            CountingInputStream cis = new CountingInputStream(copyStream);
            log.info("Importing copy stream to blobstore {}", name);
            try {
                new BlobStoreCopyParser(blobStoreAPI).parse(cis);
                size = cis.getByteCount();
            } catch (IOException e) {
                throw new IllegalStateException("Failed restoring blobstore " + name, e);
            }
        }

        // Registering in service registry

        instance.setSize(size);
    }

    public void delete(final String instanceId) {
        serviceInstancesMap.remove(instanceId);

    }

    private void add(RemoteShpanBlobInstance instance) {
        if (serviceInstancesMap.containsKey(instance.getName())) {
            throw new WebApplicationException("Blobstore " + instance.getName() + " already exists", 409);
        }
        serviceInstancesMap.put(instance.getName(), instance);
    }

    public Collection<RemoteShpanBlobInstance> list() {
        return new ArrayList<>(serviceInstancesMap.values());
    }

    public RemoteShpanBlobInstance get(String name) {
        return serviceInstancesMap.get(name);
    }

    @NoJavadoc
    // TODO add javadoc
    public void createFromCopy(String serviceId, DsbRestoreCopyInfo restoreInfo) {
        checkExists(serviceId);
        //todo: this needs to test protocol and parse accordingly
        final String copyRepoUrl = restoreInfo.getCopyRepoCredentials().get("url");
        if (copyRepoUrl == null) {
            throw new BadRequestException("Failed parsing copy repo credentials, missing \"url\"");
        }

        Response response;
        try {
            response = webAPIResolver.getWebAPI(copyRepoUrl, CrbWebDataApi.class).retrieveCopy(restoreInfo.getCopyId());
        } catch (ClientErrorException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalServerErrorException("Failed doanloading copy from copy repo " + copyRepoUrl, ex);
        }
        try {
            InputStream copyStream;
            try {
                copyStream = response.readEntity(InputStream.class);
            } catch (Exception ex) {
                throw new InternalServerErrorException("Failed reading stream form artifact", ex);
            }
            createDB(serviceId, copyStream, false);
        } finally {
            response.close();
        }

    }

    private void createCopyAndStream(
            OutputStream out, final String copyId, final String serviceName,
            final String copyPluginName) {
        BlobStore blobStore =
                Objects.requireNonNull(
                        get(serviceName).getStore(),
                        "Failed locating datasource " + serviceName + " in service registry");

        log.info("Creating copy for {}/{} with id {}", serviceName, copyId);

        long copySize = BlobstoreCopyUtil.doBlobstoreCopy(out, ServiceRegistryApi.SERVICE_TYPE_BLOBSTORE, serviceName,
                copyPluginName, copyId, blobStore);
        if (copySize == 0) {
            copySize = 50;
        }
        serviceInstancesMap.get(serviceName).setSize(copySize);
    }

    @NoJavadoc
    // TODO add javadoc
    public void copy(final String serviceName, CopyServiceInstance copyDetails) {

        try {
            final String restCopyRepoUrl = copyDetails.getCopyRepoCredentials().get("url");
            if (restCopyRepoUrl == null) {
                throw new BadRequestException("Missing url in rest copy repo credentials");
            }
            final String restCopyRepoId = copyDetails.getCopyRepoCredentials().get("repoId");
            if (restCopyRepoId == null) {
                throw new BadRequestException("Missing repoId in rest copy repo credentials");
            }

            CrbWebDataApi copyRepoAPI = webAPIResolver.getWebAPI(restCopyRepoUrl, CrbWebDataApi.class);

            PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);

            Thread thread = new Thread(
                    () -> {
                        createCopyAndStream(out, copyDetails.getCopyId(), serviceName, copyDetails.getCopyType());
                    }
            );
            thread.start();

            //todo:amit: shouldn't be two fields - desired time v.s. actual time?
            Date copyTime1 = new Date(copyDetails.getCopyTime());

            String response = copyRepoAPI.createCopyInRepo(restCopyRepoId,copyDetails.getCopyId(), in);
            thread.join();
        } catch (Exception ex) {
            //todo:amit:proper error handling
            throw new IllegalStateException(ex);
        }
    }

    @NoJavadoc
    // TODO add javadoc
    public ServiceInstanceDetails getEndpointInfo(String name) {

        RemoteShpanBlobInstance shpanBlobInstance = get(name);
        if (shpanBlobInstance == null) {
            throw new NotFoundException();
        }
        Long size = shpanBlobInstance.getSize();
        if (size == null) {
            //todo:amit:
            size = 0L;
        }

        DiscoveredService discoveredService = Objects.requireNonNull(
                serviceDiscoveryManager.discoverService(ownServiceURN), () ->
                        "failed to find own registered service " + ownServiceURN);

        String serviceURL = discoveredService.getServiceURL();
        String publicURL = discoveredService.getParams().get("publicURL");
        if (publicURL != null && !publicURL.isEmpty()) {
            serviceURL = publicURL;
        }

        Map<String, String> bind = new HashMap<>();
        bind.put("url", serviceURL);
        bind.put("serviceId", name);

        try {
            URL url = new URL(publicURL);
            int port = url.getPort();
            if (port < 0) {
                port = 80;
            }
            return new ServiceInstanceDetails(
                    name,
                    bind,
                    Collections.singletonList(new BindingPort("tcp", url.getHost(), port)),
                    "S3",
                    size,
                    ServiceInstanceDetails.StateEnum.RUNNING);

        } catch (MalformedURLException e) {
            throw new IllegalStateException("Failed binding", e);
        }
    }
}
