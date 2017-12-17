package com.emc.ocopea.docker.messaging;

import com.emc.microservice.Context;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.blobstore.BlobStoreLink;
import com.emc.microservice.messaging.MessageWriter;
import com.emc.microservice.messaging.RuntimeMessageSender;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.serialization.SerializationManager;
import com.emc.ocopea.docker.DockerDemoAppRemoteDevBlobStoreConfiguration;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/**
 * Created by liebea on 1/18/15.
 * Drink responsibly
 */
public class CFDemoAppMessageSender implements RuntimeMessageSender {
    private final CFDemoAppMessagingServer server;
    private final String queueName;
    private final BlobStoreAPI blobStoreAPI;
    private final String blobStoreNamespace;
    private final String blobHeaderKeyName;
    private final boolean gzip;

    public CFDemoAppMessageSender(
            Context context,
            ResourceProvider devResourceProvider,
            CFDemoAppMessagingServer server,
            String queueName,
            SerializationManager serializationManager,
            String blobstoreName,
            String blobNamespace,
            String blobKeyHeaderName,
            boolean gzip) {
        this(
                server,
                queueName,
                initBlobStoreAPI(blobstoreName, devResourceProvider, context),
                blobNamespace,
                blobKeyHeaderName,
                gzip);
    }

    public CFDemoAppMessageSender(
            CFDemoAppMessagingServer server,
            String queueName,
            BlobStoreAPI blobStoreAPI,
            String blobNamespace,
            String blobKeyHeaderName,
            boolean gzip) {
        this.server = server;
        this.queueName = queueName;
        this.blobStoreAPI = blobStoreAPI;
        this.blobStoreNamespace = blobNamespace == null ? CFDemoAppMessage.DEV_MESSAGING_NAMESPACE : blobNamespace;
        this.blobHeaderKeyName = blobKeyHeaderName;
        this.gzip = gzip;
    }

    private static BlobStoreAPI initBlobStoreAPI(
            String blobstoreName,
            ResourceProvider devResourceProvider,
            Context context) {
        return devResourceProvider.getBlobStore(
                new DockerDemoAppRemoteDevBlobStoreConfiguration(
                        blobstoreName,
                        "http://hahahahah"),
                context
        );
    }

    @Override
    public void streamMessage(
            final MessageWriter messageWriter,
            Map<String, String> messageHeaders,
            String messageGroup) {
        String key = getKey(messageHeaders);
        blobStoreAPI.create(blobStoreNamespace, key, messageHeaders, out -> {
            if (gzip) {
                try {
                    try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out)) {
                        messageWriter.writeMessage(gzipOutputStream);
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                messageWriter.writeMessage(out);
            }
        });
        server.sendMessage(
                queueName,
                new CFDemoAppMessageDescriptor(messageHeaders, new BlobStoreLink(blobStoreNamespace, key)));
    }

    private String getKey(Map<String, String> messageHeaders) {
        if (blobHeaderKeyName != null) {
            String key = messageHeaders.get(blobHeaderKeyName);
            if (key != null) {
                return key;
            }
        }
        return UUID.randomUUID().toString();
    }
}
