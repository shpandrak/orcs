package com.emc.ocopea.docker.datasource;

import com.emc.microservice.datasource.DatasourceProvider;
import com.emc.microservice.datasource.MicroServiceDataSource;
import com.emc.ocopea.docker.DockerDemoAppRemoteDatasourceConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with true love by liebea on 10/12/2014.
 */
public class DockerDemoAppRemoteDatasourceProvider implements DatasourceProvider<DockerDemoAppRemoteDatasourceConfiguration> {
    private Map<String, MicroServiceDataSource> datasourceByName = new HashMap<>();

    @Override
    public MicroServiceDataSource getDatasource(DockerDemoAppRemoteDatasourceConfiguration configuration) {
        return datasourceByName.computeIfAbsent(
                configuration.getDBName(),
                k -> {
                    String[] split = configuration.getURL().split("//");
                    if (split.length < 2) {
                        throw new IllegalArgumentException("url doesn't have protocol part. url: " +
                                configuration.getURL());
                    }
                    String protocol = split[0];
                    String[] protocolParts = protocol.split(":");
                    String dataBase = protocolParts[0];
                    if (dataBase.equals("jdbc")) {
                        dataBase = protocolParts[1];
                    }
                    switch (dataBase) {
                        case "h2":
                            return DockerDemoAppH2DataSource.create(
                                    configuration.getURL(),
                                    configuration.getUserName(),
                                    configuration.getPassword()
                            );
                        default:
                            throw new IllegalArgumentException("unknown protocol - " + protocol);
                    }
                }
        );
    }

    @Override
    public Class<DockerDemoAppRemoteDatasourceConfiguration> getConfClass() {
        return DockerDemoAppRemoteDatasourceConfiguration.class;
    }
}
