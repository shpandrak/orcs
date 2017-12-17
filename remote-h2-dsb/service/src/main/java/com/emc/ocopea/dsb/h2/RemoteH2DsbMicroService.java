package com.emc.ocopea.dsb.h2;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 1/4/16.
 * Drink responsibly
 */
public class RemoteH2DsbMicroService extends MicroService {
    private static final String SERVICE_NAME = "Remote H2 DSB";
    private static final String SERVICE_BASE_URI = "remote-h2-dsb";
    private static final String SERVICE_DESCRIPTION = "Remote H2 DSB Reference implementation using a single JVM";
    private static final int SERVICE_VERSION = 1;
    private static final Logger logger = LoggerFactory.getLogger(RemoteH2DsbMicroService.class);

    public RemoteH2DsbMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_BASE_URI,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()

                        .withRestResource(RemoteH2DsbResource.class, "DSB API implementation")
                        .withSingleton("h2-dsb-singleton", "singleton for fun", RemoteH2DsbSingleton.class)
                        .withParameter("externalBindHost", "External Bind Address override", null, false)
                        .withParameter("externalBindPort", "External Bind Port override", null, false)
        );

    }
}
