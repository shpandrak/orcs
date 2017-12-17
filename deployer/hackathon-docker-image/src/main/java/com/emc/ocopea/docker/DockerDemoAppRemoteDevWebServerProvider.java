package com.emc.ocopea.docker;

import com.emc.microservice.Context;
import com.emc.microservice.resource.ResourceProviderManager;
import com.emc.microservice.restapi.MicroServiceWebServer;
import com.emc.microservice.restapi.WebServerProvider;
import com.emc.microservice.standalone.web.UndertowRestEasyWebServer;
import com.emc.microservice.standalone.web.UndertowWebServerConfiguration;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by yaariy1 on 12/12/2016.
 */
public class DockerDemoAppRemoteDevWebServerProvider implements WebServerProvider<UndertowWebServerConfiguration> {
    private UndertowRestEasyWebServer undertowRestEasyWebServer = null;

    @Override
    public MicroServiceWebServer getWebServer(UndertowWebServerConfiguration configuration) {
        if (undertowRestEasyWebServer == null) {
            DockerDemoAppResourceProvider resourceProvider =
                    (DockerDemoAppResourceProvider) ResourceProviderManager.getResourceProvider();
            undertowRestEasyWebServer = new UndertowRestEasyWebServer(configuration) {
                @Override
                public void deployServiceApplication(Context context) {

                    super.deployServiceApplication(context);
                }
            };
        }
        return undertowRestEasyWebServer;

    }

    @Override
    public Class<UndertowWebServerConfiguration> getConfClass() {
        return UndertowWebServerConfiguration.class;
    }
}
