package com.emc.ocopea.demo.dsb.shpanblob;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 1/4/16.
 * Drink responsibly
 */
public class RemoteShpanBlobDsbMicroService extends MicroService {
    private static final String SERVICE_NAME = "ShpanBlob DSB";
    private static final String SERVICE_BASE_URI = "remote-shpanblob-dsb";
    private static final String SERVICE_DESCRIPTION = "Remote ShpanBlob DSB Reference implementation";
    private static final int SERVICE_VERSION = 1;
    private static final Logger logger = LoggerFactory.getLogger(RemoteShpanBlobDsbMicroService.class);

    public RemoteShpanBlobDsbMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_BASE_URI,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()

                        .withParameter("publicURL", "URL for this service", null, true)
                        .withRestResource(RemoteShpanBlobDSBResource.class, "DSB API implementation")
                        .withRestResource(RemoteShpanBlobResource.class, "DSB API implementation")
                        .withSingleton(
                                "remote-shpanblob-dsb-singleton",
                                "singleton for fun",
                                RemoteShpanBlobDSBSingleton.class)
        );

    }
}
