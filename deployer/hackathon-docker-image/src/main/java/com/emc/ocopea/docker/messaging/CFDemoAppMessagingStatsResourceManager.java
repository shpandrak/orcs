package com.emc.ocopea.docker.messaging;

import com.emc.microservice.Context;
import com.emc.microservice.health.HealthCheck;
import com.emc.microservice.messaging.MessagingStatsResourceDescriptor;
import com.emc.microservice.resource.ExternalResourceManager;
import com.emc.microservice.resource.ResourceProviderManager;

import java.util.Collection;
import java.util.Collections;

public class CFDemoAppMessagingStatsResourceManager
        implements
        ExternalResourceManager<
                MessagingStatsResourceDescriptor,
                CFDemoAppMessagingProviderConfiguration,
                CFDemoAppMessagingStatsConnection> {

    @Override
    public String getResourceTypeNamePlural() {
        return getResourceTypeName();
    }

    @Override
    public String getResourceTypeName() {
        return "Dev Messaging Statistics";
    }

    @Override
    public CFDemoAppMessagingStatsConnection initializeResource(
            MessagingStatsResourceDescriptor resourceDescriptor,
            CFDemoAppMessagingProviderConfiguration resourceConfiguration,
            Context context) {

        //noinspection unchecked
        return new CFDemoAppMessagingStatsConnection(
                resourceDescriptor,
                resourceConfiguration,
                ResourceProviderManager.getResourceProvider().getServiceRegistryApi(),
                CFDemoAppMessagingServer.getInstance());
    }

    @Override
    public void postInitResource(
            MessagingStatsResourceDescriptor resourceDescriptor,
            CFDemoAppMessagingProviderConfiguration resourceConfiguration,
            CFDemoAppMessagingStatsConnection initializedResource,
            Context context) {
    }

    @Override
    public void cleanUpResource(CFDemoAppMessagingStatsConnection resourceToCleanUp) {
    }

    @Override
    public void pauseResource(CFDemoAppMessagingStatsConnection resourceToPause) {
    }

    @Override
    public void startResource(CFDemoAppMessagingStatsConnection resourceToStart) {
    }

    @Override
    public Class<CFDemoAppMessagingProviderConfiguration> getResourceConfigurationClass() {
        return CFDemoAppMessagingProviderConfiguration.class;
    }

    @Override
    public Class<MessagingStatsResourceDescriptor> getDescriptorClass() {
        return MessagingStatsResourceDescriptor.class;
    }

    @Override
    public Collection<HealthCheck> getResourceHealthChecks(CFDemoAppMessagingStatsConnection managedResource) {
        return Collections.emptyList();
    }

}
