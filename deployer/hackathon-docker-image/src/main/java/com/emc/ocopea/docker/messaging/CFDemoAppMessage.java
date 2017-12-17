package com.emc.ocopea.docker.messaging;

import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.messaging.DefaultMessageReader;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageReader;
import com.emc.microservice.messaging.MessagingSerializationHelper;
import com.emc.microservice.serialization.SerializationManager;

import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by liebea on 1/18/15.
 * Drink responsibly
 */
public class CFDemoAppMessage implements Message {
    static final String DEV_MESSAGING_NAMESPACE = "dev-messaging";
    private final CFDemoAppMessageDescriptor descriptor;
    private final SerializationManager serializationManager;
    private final BlobStoreAPI blobStoreAPI;
    private final boolean gzip;

    public CFDemoAppMessage(
            CFDemoAppMessageDescriptor descriptor,
            SerializationManager serializationManager,
            BlobStoreAPI blobStoreAPI,
            boolean gzip) {
        this.descriptor = descriptor;
        this.serializationManager = serializationManager;
        this.blobStoreAPI = blobStoreAPI;
        this.gzip = gzip;
    }

    @Override
    public String getMessageHeader(String headerName) {
        return getMessageHeaders().get(headerName);
    }

    @Override
    public Map<String, String> getMessageHeaders() {
        return MessagingSerializationHelper.extractHeaders(descriptor.getHeaders());
    }

    @Override
    public String getContextValue(String key) {
        return getMessageContext().get(key);
    }

    @Override
    public Map<String, String> getMessageContext() {
        return MessagingSerializationHelper.extractContext(descriptor.getHeaders());
    }

    @Override
    public void readMessage(final MessageReader messageReader) {
        //read the key and get the blob from blobstore
        if (blobStoreAPI.isExists(descriptor.getBlobKey().getNamespace(), descriptor.getBlobKey().getKey())) {
            blobStoreAPI.readBlob(
                    descriptor.getBlobKey().getNamespace(),
                    descriptor.getBlobKey().getKey(),
                    in -> {
                        if (gzip) {
                            try {
                                try (GZIPInputStream gzipInputStream = new GZIPInputStream(in)) {
                                    messageReader.read(gzipInputStream);
                                }
                            } catch (IOException e) {
                                throw new IllegalStateException(e);
                            }
                        } else {
                            messageReader.read(in);
                        }
                    });
        } else {
            throw new IllegalStateException(
                    "Failed reading message with id (id doesn't exist)" + descriptor.getBlobKey());
        }
    }

    @Override
    public <T> T readObject(Class<T> format) {
        DefaultMessageReader<T> messageReader =
                new DefaultMessageReader<>(serializationManager.getReader(format), format);
        readMessage(messageReader);
        return messageReader.getResult();
    }

    @Override
    public Object getUnderlyingMessageObject() {
        return this;
    }

}
