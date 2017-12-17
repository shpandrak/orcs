package com.emc.ocopea.dsb.h2;

import com.emc.microservice.Context;
import com.emc.microservice.discovery.DiscoveredService;
import com.emc.microservice.discovery.ServiceDiscoveryManager;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.crb.CrbWebDataApi;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.dsb.BindingPort;
import com.emc.ocopea.dsb.CopyServiceInstance;
import com.emc.ocopea.dsb.DsbRestoreCopyInfo;
import com.emc.ocopea.dsb.ServiceInstanceDetails;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.NativeQueryService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.input.CountingInputStream;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RemoteH2DsbSingleton implements ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(RemoteH2DsbSingleton.class);

    private static final long EMPTY_H2_SIZE = 100L;
    private final int internalPort = 9123;
    private final Map<String, H2Instance> serviceInstancesMap = new HashMap<>();
    private int externalPort = internalPort;
    private Server server;
    private ServiceDiscoveryManager serviceDiscoveryManager;
    private String ownServiceURN;
    private WebAPIResolver webAPIResolver;
    private String externalBindHost = null;

    @Override
    public void init(Context context) {
        log.info("Initialized H2 Singleton");
        ownServiceURN = context.getMicroServiceBaseURI();
        //very platform specific, but it is what it is...
        // should be part of ms lib
        this.serviceDiscoveryManager = context.getServiceDiscoveryManager();
        this.webAPIResolver = context.getWebAPIResolver();
        try {
            server = Server.createTcpServer(
                    "-tcpPort", Integer.toString(internalPort), "-tcpAllowOthers").start();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed creating h2 db server on port " + internalPort, e);
        }

        Integer portOverride = context.getParametersBag().getInt("externalBindPort");
        if (portOverride != null) {
            externalPort = portOverride;
        }
        externalBindHost = context.getParametersBag().getString("externalBindHost");

        log.info(
                "Listening on port:{}, external port:{}, externalBindHost {}",
                internalPort,
                externalPort,
                externalBindHost);

    }

    @Override
    public void shutDown() {
        if (server != null) {
            server.shutdown();
        }
        this.server = null;

        log.info("Shutting down h2 singleton");

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
            throw new IllegalArgumentException("H2 instance by name " + name + " already exists");
        }
    }

    public void deleteDB(final String instanceId) {
        delete(get(instanceId));
    }

    private void createDB(final String name, boolean readonly) {
        H2Instance instance = new H2Instance(name, null, new Date(), readonly);
        instance.setSize(EMPTY_H2_SIZE);
        DataSource dataSource = getDS(instance);
        try (Connection connection = dataSource.getConnection()) {
            if (connection == null) {
                throw new IllegalStateException("Failed connecting to newly created db " + name);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed creating h2 db " + name, e);
        }

        add(instance);
    }

    private void createDB(final String name, final InputStream copyStream, boolean readonly) {

        H2Instance instance = new H2Instance(name, null, new Date(), readonly);

        final DataSource dataSource = getDS(instance);

        long size = EMPTY_H2_SIZE;
        // Building from copy
        if (copyStream != null) {
            log.info("Importing h2 copy stream to {}", name);

            // Read blob to db
            try {
                CountingInputStream cis = new CountingInputStream(copyStream);
                new DBCopyParser(dataSource).parse(cis);
                size = cis.getByteCount();
            } catch (IOException e) {
                throw new IllegalStateException("Failed importing copy stream for h2 service " + name, e);
            }
        }

        instance.setSize(size);
        add(instance);
    }

    private DataSource getDS(H2Instance h2Instance) {
        ServiceInstanceDetails endpointInfo = getH2EndpointInfo(h2Instance, true);
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(endpointInfo.getBinding().get("url"));
        ds.setUser(endpointInfo.getBinding().get("user"));
        ds.setPassword(endpointInfo.getBinding().get("password"));

        return ds;
    }

    @NoJavadoc
    // TODO add javadoc
    public ServiceInstanceDetails getH2EndpointInfo(H2Instance h2Instance, boolean forceLocalhost) {

        DiscoveredService discoveredService = serviceDiscoveryManager.discoverService(ownServiceURN);
        String serviceHost = "localhost";
        int portToUse;
        if (!forceLocalhost) {
            portToUse = externalPort;
            String serviceUrl = discoveredService.getServiceURL();
            if (externalBindHost != null && !externalBindHost.isEmpty()) {
                serviceHost = externalBindHost;
            } else {
                try {
                    URL ownURL = new URL(serviceUrl);
                    serviceHost = ownURL.getHost();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
        } else {
            portToUse = internalPort;
        }
        H2EndpointInfo h2EndpointInfo = new H2EndpointInfo(
                "sa",
                "",
                "jdbc:h2:tcp://" + serviceHost + ":" + portToUse + "/./target/data/" +
                        h2Instance.getName() + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
        );
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = mapper.convertValue(h2EndpointInfo, new TypeReference<HashMap<String, Object>>() {
        });
        return new ServiceInstanceDetails(
                h2Instance.getName(),
                map,
                Collections.singletonList(
                        new BindingPort("tcp", serviceHost, portToUse)
                ),
                "EBS",
                h2Instance.getSize(),
                ServiceInstanceDetails.StateEnum.RUNNING);
    }

    private void add(H2Instance instance) {
        serviceInstancesMap.put(instance.getName(), instance);
    }

    private void delete(H2Instance instance) {
        serviceInstancesMap.remove(instance.getName());
    }

    public Collection<H2Instance> list() {
        return new ArrayList<>(serviceInstancesMap.values());
    }

    public H2Instance get(String instanceId) {
        return serviceInstancesMap.get(instanceId);
    }

    @NoJavadoc
    // TODO add javadoc
    public void createFromCopy(DsbRestoreCopyInfo copyInfo, String serviceId) {
        checkExists(serviceId);
        //todo: this needs to test protocol and parse accordingly
        final String copyRepoUrl = copyInfo.getCopyRepoCredentials().get("url");
        if (copyRepoUrl == null) {
            throw new BadRequestException("Failed parsing copy repo credentials, missing \"url\"");
        }

        Response response;
        try {
            response = webAPIResolver.getWebAPI(copyRepoUrl, CrbWebDataApi.class).retrieveCopy(copyInfo.getCopyId());
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
            OutputStream out,
            final String copyId,
            final H2Instance h2Instance,
            final String copyPluginName) {
        NativeQueryService nqs = getNativeQueryService(h2Instance);
        log.info("Copying to {} datasource with id {}", h2Instance.getName(), copyId);
        final long[] copySize = {0L};

        try (final JsonGenerator generator = new JsonFactory().createGenerator(out)) {
            generator.writeStartObject();
            generator.writeStringField("copyId", copyId);
            generator.writeStringField("serviceType", "datasource");
            generator.writeStringField("serviceName", h2Instance.getName());
            generator.writeStringField("copyPluginName", copyPluginName);

            nqs.getStream("SCRIPT DROP", null, inputStream -> {
                CountingInputStream countingInputStream = new CountingInputStream(inputStream);
                generator.writeFieldName("data");
                generator.writeBinary(countingInputStream, -1);
                copySize[0] = countingInputStream.getByteCount();

            });
            generator.writeNumberField("copySize", copySize[0]);
            generator.writeEndObject();
        } catch (IOException e) {
            throw new IllegalStateException("Failed generating database copy for " + h2Instance.getName(), e);
        }

        serviceInstancesMap.get(h2Instance.getName()).setSize(copySize[0]);

    }

    @NoJavadoc
    // TODO add javadoc
    public void copy(final String serviceName, CopyServiceInstance copyDetails) {

        final H2Instance h2Instance = get(serviceName);
        if (h2Instance == null) {
            throw new NotFoundException("no instance with id " + serviceName);
        }
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
                    () -> createCopyAndStream(out, copyDetails.getCopyId(), h2Instance, copyDetails.getCopyType())
            );
            thread.start();

            //todo:amit: shouldn't be two fields - desired time v.s. actual time?
            Date copyTime1 = new Date(copyDetails.getCopyTime());

            copyRepoAPI.createCopyInRepo(restCopyRepoId, copyDetails.getCopyId(), in);
            thread.join();
        } catch (Exception ex) {
            //todo:amit:proper error handling
            throw new IllegalStateException(ex);
        }
    }

    private NativeQueryService getNativeQueryService(H2Instance h2Instance) {
        DataSource dataSource = getDS(h2Instance);
        return new BasicNativeQueryService(dataSource);
    }

}
