package com.emc.ocopea.docker;

import com.emc.microservice.Context;
import com.emc.microservice.MicroserviceIdentifier;
import com.emc.microservice.ParametersBag;
import com.emc.microservice.ServiceConfig;
import com.emc.microservice.bootstrap.AbstractSchemaBootstrap;
import com.emc.microservice.dependency.ServiceDependencyDescriptor;
import com.emc.microservice.dependency.ServiceDependencyType;
import com.emc.microservice.messaging.DestinationDescriptor;
import com.emc.microservice.messaging.InputQueueDescriptor;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.output.OutputDescriptor;
import com.emc.microservice.output.ServiceOutputDescriptor;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.registry.ServiceRegistryImpl;
import com.emc.microservice.resource.ResourceConfiguration;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 1/22/15.
 * Drink responsibly
 */
public abstract class DockerDemoAppRemoteDevModeHelper {
    private static final Logger logger = LoggerFactory.getLogger(DockerDemoAppRemoteDevModeHelper.class);

    @NoJavadoc
    // TODO add javadoc
    public static void registerServiceDependencies(
            Context context,
            int port,
            Map<String, AbstractSchemaBootstrap> schemaBootstrapMap,
            ResourceProvider devResourceProvider,
            Map<String, Map<String, Map<String, String>>> overriddenResourceProperties)
            throws IOException, SQLException {

        ServiceRegistryImpl serviceRegistryAPI =
                (ServiceRegistryImpl) devResourceProvider.getServiceRegistryApi();
        Map<String, ServiceConfig.InputQueueConfig> inputQueueConfig = new HashMap<>();
        Map<String, ServiceConfig.DestinationQueueConfig> destinationQueueConfig = new HashMap<>();

        Map<String, Map<String, String>> messagingConfiguration = new HashMap<>();

        // Registering input queues as both input and output
        for (InputQueueDescriptor currQueueDesc : context.getQueuesManager().getDescriptors()) {
            addQueueProps(messagingConfiguration, currQueueDesc.getName());
            inputQueueConfig.put(currQueueDesc.getName(), new ServiceConfig.InputQueueConfig(1, true, null));
        }

        // Setting destinations configuration
        for (DestinationDescriptor currDestinationDesc : context.getDestinationManager().getDescriptors()) {
            addQueueProps(messagingConfiguration, currDestinationDesc.getName());
            destinationQueueConfig.put(
                    currDestinationDesc.getName(),
                    new ServiceConfig.DestinationQueueConfig(null, null, true));
        }

        // Setting the service dependencies configurations
        for (final ServiceDependencyDescriptor currServiceDependencyDescriptor :
                context.getDependencyManager().getDescriptors()) {
            addServiceInputQueue(
                    messagingConfiguration,
                    currServiceDependencyDescriptor.getDependentServiceIdentifier());

            if (currServiceDependencyDescriptor.getServiceDependencyType() == ServiceDependencyType.ASYNC_CALL) {
                List<String> messageRoutingTable = currServiceDependencyDescriptor.getMessageRoutingTable();
                for (String currDependentServiceURI : messageRoutingTable) {
                    MicroserviceIdentifier serviceIdentifier = new MicroserviceIdentifier(currDependentServiceURI);
                    addServiceInputQueue(messagingConfiguration, serviceIdentifier);
                    destinationQueueConfig.put(
                            serviceIdentifier.getDefaultInputQueueName(),
                            new ServiceConfig.DestinationQueueConfig(null, null, true));
                }

                String asyncCallResultQueue = context
                        .getServiceDescriptor()
                        .getIdentifier()
                        .getDependencyCallbackQueueName(currServiceDependencyDescriptor.getLastRoute());
                addQueueProps(messagingConfiguration, asyncCallResultQueue);
                inputQueueConfig.put(asyncCallResultQueue, new ServiceConfig.InputQueueConfig(1, true, null));
            }

        }

        // Registering queues
        for (Map.Entry<String, Map<String, String>> currMessagingConf : messagingConfiguration.entrySet()) {
            logger.info("Bootstrapping messaging descriptor {}", currMessagingConf.getKey());
            Map<String, String> propertyValues = overrideResourceProperties(
                    ServiceRegistryApi.SERVICE_TYPE_QUEUE,
                    currMessagingConf.getKey(),
                    currMessagingConf.getValue(),
                    overriddenResourceProperties);
            //noinspection unchecked
            serviceRegistryAPI.registerQueue(
                    currMessagingConf.getKey(),
                    ResourceConfiguration.asSpecificConfiguration(
                            DockerDemoAppRemoteDevQueueConfiguration.class,
                            propertyValues));
        }

        OutputDescriptor outputDescriptor =
                context.getServiceDescriptor().getInitializationHelper().getOutputDescriptor();
        if (outputDescriptor != null &&
                outputDescriptor.getOutputType() == OutputDescriptor.MicroServiceOutputType.service) {
            String defaultInputQueueName =
                    new MicroserviceIdentifier(((ServiceOutputDescriptor) outputDescriptor)
                            .getServiceURI())
                            .getDefaultInputQueueName();
            destinationQueueConfig.put(
                    defaultInputQueueName,
                    new ServiceConfig.DestinationQueueConfig(null, null, true));
        }

        Map<String, ServiceConfig.DataSourceConfig> dataSourceConfig = new HashMap<>();
        Map<String, ServiceConfig.DataSourceConfig> blobstoreConfig = new HashMap<>();
        serviceRegistryAPI.registerServiceConfig(
                context.getMicroServiceBaseURI(),
                ServiceConfig.generateServiceConfig(
                        context.getMicroServiceBaseURI(),
                        null,
                        "http://" + devResourceProvider.getNodeAddress() + ":" + port + "/" +
                                context.getServiceDescriptor().getIdentifier().getRestURI(),
                        "DEBUG",
                        Collections.emptyMap(),
                        inputQueueConfig,
                        destinationQueueConfig,
                        dataSourceConfig,
                        blobstoreConfig,
                        loadServiceParameters(context),
                        new HashMap<>()));

    }

    private static Map<String, String> overrideResourceProperties(
            String type,
            String name,
            Map<String, String> propertyValues,
            Map<String, Map<String, Map<String, String>>> overriddenResourceProperties) {
        Map<String, String> props = new HashMap<>(propertyValues);
        Map<String, Map<String, String>> byType = overriddenResourceProperties.get(type);
        if (byType != null) {
            Map<String, String> byName = byType.get(name);
            if (byName != null) {
                props.putAll(byName);
            }
        }
        return props;

    }

    private static Map<String, String> loadServiceParameters(Context context) {
        Map<String, String> serviceParameterValues = new HashMap<>();
        for (Map.Entry<String, ParametersBag.MicroServiceParameterDescriptor> currParamEntry :
                context.getParametersBag().getParameterDescriptorsMap().entrySet()) {
            String propertyValue = System.getProperty(context.getMicroServiceBaseURI() + "_" + currParamEntry.getKey());
            if (propertyValue != null) {
                logger.info("Overriding property value {} with {}", currParamEntry.getKey(), propertyValue);
            }
            serviceParameterValues.put(currParamEntry.getKey(), propertyValue);

        }
        return serviceParameterValues;
    }

    private static void addQueueProps(Map<String, Map<String, String>> messagingConfiguration, String queueURI) {
        Map<String, String> messagingProps = messagingConfiguration.get(queueURI);
        if (messagingProps == null) {
            messagingProps = new HashMap<>();
            messagingConfiguration.put(queueURI, messagingProps);
        }
        messagingProps.putAll(
                new DockerDemoAppRemoteDevQueueConfiguration(QueueConfiguration.MessageDestinationType.QUEUE)
                        .getPropertyValues()
        );
    }

    private static void addServiceInputQueue(
            Map<String, Map<String, String>> messagingConfiguration,
            MicroserviceIdentifier serviceIdentifier) {
        String standardInputQueueName = serviceIdentifier.getDefaultInputQueueName();
        addQueueProps(messagingConfiguration, standardInputQueueName);
    }

}
