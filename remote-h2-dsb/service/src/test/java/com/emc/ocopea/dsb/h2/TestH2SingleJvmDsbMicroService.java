package com.emc.ocopea.dsb.h2;

import com.emc.microservice.testing.MicroServiceTestHelper;
import com.emc.ocopea.dsb.CreateServiceInstance;
import com.emc.ocopea.dsb.DsbInfo;
import com.emc.ocopea.dsb.DsbWebApi;
import com.emc.ocopea.dsb.ServiceInstanceDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

public class TestH2SingleJvmDsbMicroService {

    private MicroServiceTestHelper microServiceTestHelper;

    @Before
    public void init() throws SQLException, IOException {
        microServiceTestHelper = new MicroServiceTestHelper(new RemoteH2DsbMicroService());

        // Creating the bank db schema
        //microServiceTestHelper.createSchema(new ());

        // Starting the service in test mode
        microServiceTestHelper.startServiceInTestMode();

    }

    @After
    public void tearDown() {
        microServiceTestHelper.stopTestMode();
    }

    @Test
    public void testBasicShit() throws Exception {

        DsbWebApi dsbWebAPI = microServiceTestHelper.getServiceResource(RemoteH2DsbResource.class);
        final DsbInfo dsbInfo = dsbWebAPI.getDSBInfo();
        Assert.assertNotNull(dsbInfo);
        System.out.println(dsbInfo);
        dsbWebAPI.createServiceInstance(new CreateServiceInstance("myDB", new HashMap<>()));
        final ServiceInstanceDetails myDB = dsbWebAPI.getServiceInstance("myDB");
        H2EndpointInfo endpointInfo = new ObjectMapper().convertValue(myDB.getBinding(), H2EndpointInfo.class);
        DataSource db = getDS(endpointInfo);
        try (Connection c = db.getConnection()) {
            Assert.assertTrue(c.isValid(3000));
        }

    }

    private DataSource getDS(H2EndpointInfo endpointInfo) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(endpointInfo.getUrl());
        //ds.setURL(MessageFormat.format(endpointInfo.getUrl(), "localhost"));
        ds.setUser(endpointInfo.getUser());
        ds.setPassword(endpointInfo.getPassword());

        return ds;

    }
}
