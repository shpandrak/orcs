package com.emc.ocopea.docker.messaging;

import com.emc.microservice.messaging.MessagingProviderConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;

import java.util.Collections;

/**
 * Created by liebea on 2/22/15.
 * Drink responsibly
 */
public class CFDemoAppMessagingProviderConfiguration extends MessagingProviderConfiguration {
    private static final String CONFIGURATION_NAME = "Dev Messaging Statistics";

    public CFDemoAppMessagingProviderConfiguration() {
        super(CONFIGURATION_NAME, Collections.<ResourceConfigurationProperty>emptyList());
    }

    @Override
    public String getMessagingNode() {
        return "localhost";
    }
}
