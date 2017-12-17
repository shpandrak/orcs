package com.emc.nazgul.demo;

import com.emc.dpa.dev.DevResourceProvider;
import com.emc.microservice.runner.MicroServiceRunner;
import com.emc.ocopea.demo.dsb.shpanblob.RemoteShpanBlobDsbMicroService;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by liebea on 11/30/16.
 * Drink responsibly
 */
public class RemoteShpanBlobDSBDevMain {
    public static void main(String[] args) throws IOException, SQLException {
        DevResourceProvider resourceProvider = new DevResourceProvider();
        new MicroServiceRunner().run(resourceProvider, new RemoteShpanBlobDsbMicroService());

    }
}
