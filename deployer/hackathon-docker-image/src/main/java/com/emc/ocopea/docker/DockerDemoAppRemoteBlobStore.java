package com.emc.ocopea.docker;

import com.emc.microservice.blobstore.BlobReader;
import com.emc.microservice.blobstore.BlobStore;
import com.emc.microservice.blobstore.BlobStoreLink;
import com.emc.microservice.blobstore.BlobWriter;
import com.emc.microservice.blobstore.DuplicateObjectKeyException;
import com.emc.microservice.blobstore.IllegalStoreStateException;
import com.emc.microservice.blobstore.ObjectKeyFormatException;
import com.emc.ocopea.util.io.StreamUtil;
import org.apache.http.HttpStatus;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by liebea on 6/26/16.
 * Drink responsibly
 */
public class DockerDemoAppRemoteBlobStore implements BlobStore {
    private final ShpanBlobRemoteWebAPILocalCopy api;
    private final String serviceId;

    public DockerDemoAppRemoteBlobStore(ShpanBlobRemoteWebAPILocalCopy api, String serviceId) {
        this.api = api;
        this.serviceId = serviceId;
    }

    @Override
    public void create(String namespace, String key, Map<String, String> headers, InputStream blob)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException {
        api.createBlob(serviceId, namespace, key, blob).close();
    }

    @Override
    public void create(String namespace, String key, Map<String, String> headers, BlobWriter blobWriter)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException {
        //todo:ugly in memory impl
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            blobWriter.write(byteArrayOutputStream);
            try (ByteArrayInputStream byteArrayInputStream =
                         new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
                api.createBlob(serviceId, namespace, key, byteArrayInputStream).close();
            }
        } catch (IOException e) {
            throw new IllegalStateException(key);
        }
    }

    @Override
    public void update(String namespace, String key, Map<String, String> headers, InputStream blob)
            throws ObjectKeyFormatException, IllegalStoreStateException {
        api.updateBlob(serviceId, namespace, key, blob).close();
    }

    @Override
    public void update(String namespace, String key, Map<String, String> headers, BlobWriter blobWriter)
            throws ObjectKeyFormatException, IllegalStoreStateException {
        //todo:ugly in memory impl
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            blobWriter.write(byteArrayOutputStream);
            try (ByteArrayInputStream byteArrayInputStream =
                         new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
                api.updateBlob(serviceId, namespace, key, byteArrayInputStream).close();
            }
        } catch (IOException e) {
            throw new IllegalStateException(key);
        }
    }

    @Override
    public void moveNameSpace(String oldNamespace, String key, String newNamespace)
            throws ObjectKeyFormatException, DuplicateObjectKeyException, IllegalStoreStateException {
        throw new UnsupportedOperationException("hahahaha");
    }

    @Override
    public void readBlob(String namespace, String key, OutputStream out)
            throws ObjectKeyFormatException, IllegalStoreStateException {
        Response response = api.readBlob(serviceId, namespace, key);
        try (InputStream inputStream = response.readEntity(InputStream.class)) {
            StreamUtil.copy(inputStream, out);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            response.close();
        }
    }

    @Override
    public void readBlob(String namespace, String key, BlobReader reader)
            throws ObjectKeyFormatException, IllegalStoreStateException {
        Response response = api.readBlob(serviceId, namespace, key);
        try (InputStream inputStream = response.readEntity(InputStream.class)) {
            reader.read(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            response.close();
        }

    }

    @Override
    public Map<String, String> readHeaders(String namespace, String key)
            throws ObjectKeyFormatException, IllegalStoreStateException {
        throw new UnsupportedOperationException("hahahaha");
    }

    @Override
    public void delete(String namespace, String key) throws ObjectKeyFormatException, IllegalStoreStateException {
        api.delete(serviceId, namespace, key).close();
    }

    @Override
    public void delete(int expirySeconds) throws IllegalStoreStateException {
    }

    @Override
    public boolean isExists(String namespace, String key) throws IllegalStoreStateException {
        Response exists = null;
        try {
            exists = api.isExists(serviceId, namespace, key);
            return exists.getStatus() == HttpStatus.SC_OK;
        } finally {
            if (exists != null) {
                exists.close();
            }
        }
    }

    @Override
    public Collection<BlobStoreLink> list() {
        return api.list(serviceId)
                .stream()
                .map(
                        shpanBlobKeyDTO ->
                                new BlobStoreLink(
                                        shpanBlobKeyDTO.getNamespace(),
                                        shpanBlobKeyDTO.getKey()))
                .collect(Collectors.toList());
    }

}
