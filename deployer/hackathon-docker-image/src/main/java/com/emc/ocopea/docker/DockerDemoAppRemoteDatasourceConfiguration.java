package com.emc.ocopea.docker;

import com.emc.microservice.datasource.DatasourceConfiguration;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;

import java.util.Arrays;
import java.util.List;

/**
 * Created by liebea on 2/6/15.
 * Drink responsibly
 */
public class DockerDemoAppRemoteDatasourceConfiguration extends DatasourceConfiguration {
    private static final String CONFIGURATION_NAME = "Remote Datasource";
    private static final ResourceConfigurationProperty PROPERTY_DBNAME = new ResourceConfigurationProperty(
            "dbName",
            ResourceConfigurationPropertyType.STRING,
            "Database Name",
            true,
            false);
    private static final ResourceConfigurationProperty PROPERTY_USER = new ResourceConfigurationProperty(
            "userName",
            ResourceConfigurationPropertyType.STRING,
            "User Name",
            true,
            true);
    private static final ResourceConfigurationProperty PROPERTY_PASSWORD = new ResourceConfigurationProperty(
            "password",
            ResourceConfigurationPropertyType.STRING,
            "password",
            false,
            true);
    private static final ResourceConfigurationProperty PROPERTY_URL = new ResourceConfigurationProperty(
            "url",
            ResourceConfigurationPropertyType.STRING,
            "DB Connection String",
            true,
            false);
    private static final ResourceConfigurationProperty PROPERTY_LOGICAL_NAME = new ResourceConfigurationProperty(
            "logicalName",
            ResourceConfigurationPropertyType.STRING,
            "Logical name, shared among copies",
            true,
            false);

    private static final List<ResourceConfigurationProperty> PROPERTIES =
            Arrays.asList(PROPERTY_DBNAME, PROPERTY_URL, PROPERTY_PASSWORD, PROPERTY_URL, PROPERTY_LOGICAL_NAME);

    public DockerDemoAppRemoteDatasourceConfiguration() {
        super(CONFIGURATION_NAME, PROPERTIES);
    }

    public DockerDemoAppRemoteDatasourceConfiguration(
            String dbName,
            String userName,
            String password,
            String url,
            String logicalName) {
        this();
        setPropertyValues(propArrayToMap(new String[]{
                PROPERTY_DBNAME.getName(), dbName,
                PROPERTY_USER.getName(), userName,
                PROPERTY_PASSWORD.getName(), password,
                PROPERTY_URL.getName(), url,
                PROPERTY_LOGICAL_NAME.getName(), logicalName
        }));
    }

    @NoJavadoc
    // TODO add javadoc
    public static String makeSchemaSafe(String dsName) {
        String s = dsName.replaceAll("-", "_");
        s = s.replaceAll("\\W", "");
        if (Character.isDigit(s.charAt(0))) {
            s = "_" + s;
        }
        return s;
    }

    @Override
    public String getDatabaseSchema() {
        return makeSchemaSafe(getLogicalName());
    }

    public String getDBName() {
        return getProperty(PROPERTY_DBNAME.getName());
    }

    public String getUserName() {
        return getProperty(PROPERTY_USER.getName());
    }

    public String getPassword() {
        return getProperty(PROPERTY_PASSWORD.getName());
    }

    public String getURL() {
        return getProperty(PROPERTY_URL.getName());
    }

    public String getLogicalName() {
        return getProperty(PROPERTY_LOGICAL_NAME.getName());
    }

}
