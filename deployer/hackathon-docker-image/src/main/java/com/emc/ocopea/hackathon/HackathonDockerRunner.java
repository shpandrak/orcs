package com.emc.ocopea.hackathon;

import com.emc.microservice.bootstrap.AbstractSchemaBootstrap;
import com.emc.microservice.runner.MicroServiceRunner;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.docker.DockerDemoAppResourceProvider;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liebea on 7/21/15.
 * Drink responsibly
 */
public class HackathonDockerRunner {

    @NoJavadoc
    public static void main(String[] args) throws IOException, SQLException {
        detectProMode();

        Map<String, AbstractSchemaBootstrap> schemaBootstrapMap = new HashMap<>();
        schemaBootstrapMap.put("hackathon-db", new HackathonSubmissionSchemaBootstrap());
        DockerDemoAppResourceProvider devResourceProvider = new DockerDemoAppResourceProvider(schemaBootstrapMap);
        new MicroServiceRunner().run(
                devResourceProvider,
                new HackathonSubmissionMicroService());
    }

    private static void detectProMode() {
        final String proModeStr = System.getenv("HACKATHON-PRO-MODE");
        System.setProperty(
                "hackathon_hackathon-pro-mode",
                String.valueOf(proModeStr != null && Boolean.valueOf(proModeStr)));
    }

}
