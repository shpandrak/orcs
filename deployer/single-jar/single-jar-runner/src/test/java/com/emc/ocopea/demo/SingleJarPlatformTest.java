package com.emc.ocopea.demo;

import com.emc.ocopea.scenarios.CheckAppTemplateExistScenario;
import com.emc.ocopea.scenarios.CreateSavedImageScenario;
import com.emc.ocopea.scenarios.DeployApplicationScenario;
import com.emc.ocopea.scenarios.DeployApplicationWhenDuplicateScenario;
import com.emc.ocopea.scenarios.DeploySavedImageScenario;
import com.emc.ocopea.scenarios.DeployTestDevApplicationScenario;
import com.emc.ocopea.scenarios.GetLatestCopyScenario;
import com.emc.ocopea.scenarios.GetRandomSiteScenario;
import com.emc.ocopea.scenarios.NavigateToDashboardScenario;
import com.emc.ocopea.scenarios.RepurposeScenario;
import com.emc.ocopea.scenarios.ScenarioRunner;
import com.emc.ocopea.scenarios.SimpleServiceWebTargetResolver;
import com.emc.ocopea.scenarios.StopAppInstanceScenario;
import com.emc.ocopea.scenarios.WaitForCopyToBeInCreatedState;
import com.emc.ocopea.scenarios.WaitForSaveImageToCreate;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by liebea on 6/20/16.
 * Drink responsibly
 */

public class SingleJarPlatformTest {
    private static String hubWebAPIURL;

    @BeforeClass
    public static void beforeClass() throws IOException, SQLException {
        hubWebAPIURL = SingleJarDemoMain.run();
    }

    @Test
    public void deployHackathonTest() throws IOException, SQLException, InterruptedException {
        try (SimpleServiceWebTargetResolver urlResolver = getResolver()) {
            new ScenarioRunner(
                    urlResolver, new ScenarioRunner.ScenariosExecution(
                    new CheckAppTemplateExistScenario("hackathon", "appTemplate.hackathon.id"))
                    .then(new GetRandomSiteScenario("site.id"))
                    .then(new DeployApplicationScenario(
                            "hack1",
                            "appTemplate.hackathon.id",
                            "site.id",
                            "appInstance.hack1.id"))
                    .then(new DeployApplicationWhenDuplicateScenario("hack1", "appTemplate.hackathon.id", "site.id"))
                    .then(new NavigateToDashboardScenario("appInstance.hack1.id"))
            ).run();
        }
    }

    public SimpleServiceWebTargetResolver getResolver() {
        return SimpleServiceWebTargetResolver.builder()
                .withService("hub-web", hubWebAPIURL)
                .withTargetRegister(new BasicAuthentication("shpandrak", "1234"))
                .build();
    }

    @Test
    public void deployHackathonTestDevTest() throws IOException, SQLException, InterruptedException {
        try (SimpleServiceWebTargetResolver urlResolver = getResolver()) {
            new ScenarioRunner(
                    urlResolver, new ScenarioRunner.ScenariosExecution(
                    new CheckAppTemplateExistScenario("hackathon", "appTemplate.hackathon.id"))
                    .then(new GetRandomSiteScenario("site.id"))
                    .then(new DeployTestDevApplicationScenario(
                            "hack-td1",
                            "appTemplate.hackathon.id",
                            "site.id",
                            "appInstance.hack-td1.id"))
            ).run();
        }
    }

    @Test
    public void stopHackathonTestDevTest() throws IOException, SQLException, InterruptedException {
        try (SimpleServiceWebTargetResolver urlResolver = getResolver()) {
            new ScenarioRunner(
                    urlResolver, new ScenarioRunner.ScenariosExecution(
                    new CheckAppTemplateExistScenario("hackathon", "appTemplate.hackathon.id"))
                    .then(new GetRandomSiteScenario("site.id"))
                    .then(new DeployTestDevApplicationScenario(
                            "hack-td2",
                            "appTemplate.hackathon.id",
                            "site.id",
                            "appInstance.hack-td2.id"))
                    .then(new StopAppInstanceScenario("appInstance.hack-td2.id"))
            ).run();
        }
    }

    @Test
    public void createAndDeploySavedImageTest() throws IOException, SQLException {
        try (SimpleServiceWebTargetResolver urlResolver = getResolver()) {
            new ScenarioRunner(
                    urlResolver, new ScenarioRunner.ScenariosExecution(
                    new CheckAppTemplateExistScenario("hackathon", "appTemplate.hackathon.id"))
                    .then(new GetRandomSiteScenario("site.id"))
                    .then(new DeployTestDevApplicationScenario(
                            "hack-td3",
                            "appTemplate.hackathon.id",
                            "site.id",
                            "appInstance.hack-td3.id"))
                    .then(new CreateSavedImageScenario(
                            "myFirstImage",
                            new HashSet<>(Arrays.asList("test/dev", "customer", "perf")),
                            "wooo image",
                            "appInstance.hack-td3.id",
                            "savedImage.myFirstImage.id"))
                    .then(new WaitForSaveImageToCreate("savedImage.myFirstImage.id", 35))
                    .then(new DeploySavedImageScenario(
                            "from-saved-image",
                            "savedImage.myFirstImage.id",
                            "site.id",
                            "appInstance.from-saved-image.id"))
            ).run();
        }
    }

    @Test
    public void repurposeCopyTest() throws IOException, SQLException {
        try (SimpleServiceWebTargetResolver urlResolver = getResolver()) {
            String appInstanceIdKey = "appInstance.hackathon.id";
            String imageName = "hackathon-image";
            String copyInstanceIdKey = "appInstance." + imageName + ".id";

            new ScenarioRunner(
                    urlResolver, new ScenarioRunner.ScenariosExecution(
                    new CheckAppTemplateExistScenario("hackathon", "appTemplate.hackathon.id"))
                    .then(new GetRandomSiteScenario("site.id"))
                    .then(new DeployApplicationScenario(
                            "hackathon",
                            "appTemplate.hackathon.id",
                            "site.id",
                            "appInstance.hackathon.id"))
                    .then(new GetLatestCopyScenario(appInstanceIdKey, copyInstanceIdKey))
                    .then(new WaitForCopyToBeInCreatedState(appInstanceIdKey, copyInstanceIdKey, 25))
                    .then(new RepurposeScenario(
                            "hackathon-restored",
                            "test/dev",
                            appInstanceIdKey,
                            copyInstanceIdKey,
                            "appInstance.hackathon-restored.id"))
            ).run();
        }
    }
}
