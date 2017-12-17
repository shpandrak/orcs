package com.emc.ocopea.docker.messaging;

import com.emc.microservice.messaging.MessagingStatsConnection;
import com.emc.microservice.messaging.MessagingStatsResourceDescriptor;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.messaging.QueueStats;
import com.emc.microservice.registry.ServiceRegistryApi;

/**
 * Created by liebea on 2/22/15.
 * Drink responsibly
 */
public class CFDemoAppMessagingStatsConnection
        extends MessagingStatsConnection<CFDemoAppMessagingProviderConfiguration, QueueConfiguration> {

    private final CFDemoAppMessagingServer messagingServer;

    protected CFDemoAppMessagingStatsConnection(
            MessagingStatsResourceDescriptor descriptor,
            CFDemoAppMessagingProviderConfiguration messagingConfiguration,
            ServiceRegistryApi registryAPI,
            CFDemoAppMessagingServer messagingServer) {
        super(
                descriptor,
                messagingConfiguration,
                registryAPI,
                CFDemoAppMessagingProviderConfiguration.class,
                QueueConfiguration.class);
        this.messagingServer = messagingServer;
    }

    @Override
    protected QueueStats getQueueStats(
            String queueName,
            CFDemoAppMessagingProviderConfiguration destinationConfiguration,
            QueueConfiguration devQueueConfiguration) {
        CFDemoAppMessagingServer.DevMessagingStats devMessageStats = messagingServer.getMessageStats(queueName);
        return new QueueStats(
                devMessageStats.getName(),
                devMessageStats.getMessagesInQueue(),
                devMessageStats.getMessagesSinceRestart());
    }
}
