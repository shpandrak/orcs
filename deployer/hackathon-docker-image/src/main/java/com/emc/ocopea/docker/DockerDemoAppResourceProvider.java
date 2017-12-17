/* $Id$
 *
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */

package com.emc.ocopea.docker;

import com.emc.microservice.Context;
import com.emc.microservice.blobstore.BlobStoreConfiguration;
import com.emc.microservice.bootstrap.AbstractSchemaBootstrap;
import com.emc.microservice.config.ConfigurationAPI;
import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.registry.ServiceRegistryImpl;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.resource.ResourceProviderManager;
import com.emc.microservice.schedule.SchedulerConfiguration;
import com.emc.microservice.standalone.web.UndertowWebServerConfiguration;
import com.emc.ocopea.microservice.schedule.SchedulerApi;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author shresa This resource provider can be used to connect to a remote dev server
 */
public class DockerDemoAppResourceProvider extends ResourceProvider {
    private final Map<String, AbstractSchemaBootstrap> schemaBootstrapMap;
    private final Map<String, Map<String, Map<String, String>>> overridenResourceProperties = new HashMap<>();

    public DockerDemoAppResourceProvider() throws IOException, SQLException {
        this(Collections.emptyMap());
    }

    public DockerDemoAppResourceProvider(Map<String, AbstractSchemaBootstrap> schemaBootstrapMap)
            throws IOException, SQLException {
        this(schemaBootstrapMap, new DockerDemoAppModeConfigurationImpl());
    }

    private static String readEnv(String envName) {
        return Objects.requireNonNull(
                System.getenv(envName),
                envName + " environment variable does not exist");
    }

    private DockerDemoAppResourceProvider(
            Map<String, AbstractSchemaBootstrap> schemaBootstrapMap,
            ConfigurationAPI configurationAPI) throws IOException, SQLException {
        super(configurationAPI, new ServiceRegistryImpl(configurationAPI) {

            @Override
            public <DatasourceConfT extends DatasourceConfiguration> DatasourceConfT getDataSourceConfiguration(
                    Class<DatasourceConfT> confClass,
                    String dataSourceName) {

                //noinspection unchecked
                return confClass.cast(
                        new DockerDemoAppRemoteDatasourceConfiguration(
                                dataSourceName,
                                readEnv("DB_USER"),
                                readEnv("DB_PASSWORD"),
                                readEnv("DB_URL"),
                                dataSourceName));
            }

            @Override
            public <BlobstoreConfT extends BlobStoreConfiguration> BlobstoreConfT getBlobStoreConfiguration(
                    Class<BlobstoreConfT> confClass,
                    String blobstoreName) {

                return confClass.cast(
                        new DockerDemoAppRemoteDevBlobStoreConfiguration(
                                readEnv("BLOB_SERVICE_ID"),
                                readEnv("BLOB_URL")));
            }
        });
        ResourceProviderManager.setResourceProvider(this);

        int port = 8080;

        this.schemaBootstrapMap = new HashMap<>(schemaBootstrapMap);

        // Registering web server configuration
        getServiceRegistryApi().registerWebServer(
                "default",
                new UndertowWebServerConfiguration(port));
    }

    public void overrideResourceProperties(String type, String name, String propName, String propValue) {
        Map<String, Map<String, String>> byType =
                overridenResourceProperties.computeIfAbsent(type, k -> new HashMap<>());
        Map<String, String> byName = byType.computeIfAbsent(name, k -> new HashMap<>());
        byName.put(propName, propValue);
    }

    public Map<String, Map<String, Map<String, String>>> getOverridenResourceProperties() {
        return overridenResourceProperties;
    }

    @Override
    public SchedulerApi getScheduler(SchedulerConfiguration schedulerConfiguration, Context context) {
        throw new UnsupportedOperationException("Who added this to the interface nad not the implementation?");
    }

    @Override
    public final String getNodeAddress() {
        return "localhost";
    }

    @Override
    public void preRunServiceHook(Context context) {
        try {
            DockerDemoAppRemoteDevModeHelper.registerServiceDependencies(
                    context,
                    8080,
                    getSchemaBootstrapMap(),
                    this,
                    getOverridenResourceProperties());

        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Failed setting up dev-mode", e);
        }
    }

    public Map<String, AbstractSchemaBootstrap> getSchemaBootstrapMap() {
        return new HashMap<>(schemaBootstrapMap);
    }
}
