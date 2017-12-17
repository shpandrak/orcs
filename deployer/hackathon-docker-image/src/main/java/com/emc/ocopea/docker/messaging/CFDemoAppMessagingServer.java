package com.emc.ocopea.docker.messaging;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by liebea on 1/18/15.
 * Drink responsibly
 */
public class CFDemoAppMessagingServer {
    public static final String DEV_MESSAGING_BLOBSTORE_NAME = "dev-messaging";
    private static final CFDemoAppMessagingServer instance = new CFDemoAppMessagingServer();
    private final Map<String, BlockingQueue<CFDemoAppMessageDescriptor>> messages = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> messagesSinceStartup = new ConcurrentHashMap<>();

    private CFDemoAppMessagingServer() {
    }

    public static CFDemoAppMessagingServer getInstance() {
        return instance;
    }

    public CFDemoAppMessageDescriptor receive(String queueName) throws InterruptedException {
        BlockingQueue<CFDemoAppMessageDescriptor> queue = getQueue(queueName);
        return queue.take();
    }

    private synchronized BlockingQueue<CFDemoAppMessageDescriptor> getQueue(String queueName) {
        BlockingQueue<CFDemoAppMessageDescriptor> queue = messages.get(queueName);
        if (queue == null) {
            queue = new ArrayBlockingQueue<>(1000);
            messages.put(queueName, queue);
            messagesSinceStartup.put(queueName, new AtomicLong(0));
        }
        return queue;
    }

    @NoJavadoc
    // TODO add javadoc
    public void sendMessage(String queueName, CFDemoAppMessageDescriptor cfDemoAppMessageDescriptor) {
        BlockingQueue<CFDemoAppMessageDescriptor> queue = getQueue(queueName);
        messagesSinceStartup.get(queueName).getAndIncrement();
        try {
            queue.put(cfDemoAppMessageDescriptor);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Failed putting message in queue", e);
        }
    }

    @NoJavadoc
    // TODO add javadoc
    public DevMessagingStats getMessageStats(String queueName) {

        AtomicLong atomicLong = messagesSinceStartup.get(queueName);
        if (atomicLong == null) {
            return new DevMessagingStats(queueName, 0L, 0L);
        }
        BlockingQueue<CFDemoAppMessageDescriptor> queue = getQueue(queueName);

        return new DevMessagingStats(queueName, queue.size(), atomicLong.get());
    }

    static final class DevMessagingStats {
        private String name;
        private long messagesInQueue;
        private long messagesSinceRestart;

        DevMessagingStats(String name, long messagesInQueue, long messagesSinceRestart) {
            this.name = name;
            this.messagesInQueue = messagesInQueue;
            this.messagesSinceRestart = messagesSinceRestart;
        }

        public String getName() {
            return name;
        }

        public long getMessagesInQueue() {
            return messagesInQueue;
        }

        public long getMessagesSinceRestart() {
            return messagesSinceRestart;
        }
    }
}
