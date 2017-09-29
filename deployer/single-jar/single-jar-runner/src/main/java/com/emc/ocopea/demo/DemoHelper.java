package com.emc.ocopea.demo;

import com.emc.microservice.discovery.ServiceDiscoveryManager;
import com.emc.microservice.discovery.WebAPIConnection;
import com.emc.microservice.resource.DefaultWebApiResolver;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.microservice.webclient.WebApiResolverBuilder;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.hub.application.HubWebApi;
import com.emc.ocopea.hub.site.AddSiteToHubCommandArgs;
import com.emc.ocopea.hub.webapp.HubWebAppImageStoreWebApi;
import com.emc.ocopea.hub.webapp.HubWebAppTestDevWebApi;
import com.emc.ocopea.hub.webapp.HubWebAppUserWebApi;
import com.emc.ocopea.hub.webapp.HubWebAppWebApi;
import com.emc.ocopea.hub.webapp.UIAppInstanceState;
import com.emc.ocopea.hub.webapp.UIAppServiceConfiguration;
import com.emc.ocopea.hub.webapp.UIAppServiceDeploymentPlan;
import com.emc.ocopea.hub.webapp.UIAppTemplateConfigurationForSite;
import com.emc.ocopea.hub.webapp.UIAppTemplateDeploymentPlan;
import com.emc.ocopea.hub.webapp.UIApplicationPolicyInfo;
import com.emc.ocopea.hub.webapp.UIApplicationTemplate;
import com.emc.ocopea.hub.webapp.UICommandAddJiraIntegration;
import com.emc.ocopea.hub.webapp.UICommandCreateAppTemplate;
import com.emc.ocopea.hub.webapp.UICommandDeployAppArgs;
import com.emc.ocopea.hub.webapp.UIDSBConfiguration;
import com.emc.ocopea.hub.webapp.UIDSBPlanConfiguration;
import com.emc.ocopea.hub.webapp.UIDataServiceConfiguration;
import com.emc.ocopea.hub.webapp.UIDataServiceDeploymentPlan;
import com.emc.ocopea.hub.webapp.UISignInInfo;
import com.emc.ocopea.hub.webapp.UISiteTopology;
import com.emc.ocopea.hub.webapp.UIUser;
import com.emc.ocopea.protection.AppCopyManager;
import com.emc.ocopea.protection.ProtectApplicationInstanceInfoDTO;
import com.emc.ocopea.site.AddCustomArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.AddMavenArtifactRegistryToSiteCommandArgs;
import com.emc.ocopea.site.RegisterCrbToSiteCommandArgs;
import com.emc.ocopea.site.RegisterDsbToSiteCommandArgs;
import com.emc.ocopea.site.RegisterPsbToSiteCommandArgs;
import com.emc.ocopea.site.SiteWebApi;
import com.emc.ocopea.site.app.DeployedApplication;
import com.emc.ocopea.site.app.DeployedApplicationLoader;
import com.emc.ocopea.site.app.PolicyDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class DemoHelper {
    private final ServiceDiscoveryManager serviceDiscoveryManager;
    private final DeployedApplicationLoader deployedApplicationLoader;

    public DemoHelper(
            ServiceDiscoveryManager serviceDiscoveryManager,
            DeployedApplicationLoader deployedApplicationLoader) {
        this.serviceDiscoveryManager = serviceDiscoveryManager;
        this.deployedApplicationLoader = deployedApplicationLoader;
    }

    @NoJavadoc
    // TODO add javadoc
    public void runDemoApp(AppCopyManager appCopyManager) {
        // Deploying the app
        String appInstanceName = "hack-prod";

        signIn();
        // Deploying the app

        final UUID appInstanceId = deployApp(appInstanceName);

        final DeployedApplication load = deployedApplicationLoader.load(appInstanceId);

        // Tempering with fake historical copies
        appCopyManager.fakeHistoricalCopies(
                new ProtectApplicationInstanceInfoDTO(
                        PolicyDeployer.buildProtectDataServiceBindings(load, Collections.emptyMap()),
                        -1,
                        appInstanceId.toString(),
                        PolicyDeployer.buildProtectAppConfigurations(load)));

        signOut();
    }

    private void signIn() {
        getHubWebAppUserApi().signIn2(new UISignInInfo("shpandrak", "1234"));
    }

    private HubWebAppUserWebApi getHubWebAppUserApi() {
        return serviceDiscoveryManager.discoverServiceConnection("hub-web").resolve(HubWebAppUserWebApi.class);
    }

    private void signOut() {
        getHubWebAppUserApi().signOut();
    }

    private UUID deployApp(String appInstanceName) {
        WebAPIConnection hubWebAppConnection = serviceDiscoveryManager.discoverServiceConnection("hub-web");

        final Collection<UIApplicationTemplate> uiApplicationTemplates =
                hubWebAppConnection.resolve(HubWebAppWebApi.class).listAppTemplates();
        final UUID hackathonAppTemplateId = uiApplicationTemplates
                .stream()
                .filter(uiApplicationTemplate -> uiApplicationTemplate.getName().equalsIgnoreCase("hackathon"))
                .findFirst()
                .get()
                .getId();

        final UISiteTopology siteTopology =
                hubWebAppConnection.resolve(HubWebAppWebApi.class).listSiteTopologies().iterator().next();
        final UUID siteId = siteTopology.getId();
        final String space = siteTopology.getSpaces().iterator().next();

        final UIAppTemplateConfigurationForSite appTemplateConfigurationBySite =
                hubWebAppConnection.resolve(HubWebAppTestDevWebApi.class)
                        .getAppTemplateConfigurationBySite(siteId, hackathonAppTemplateId);

        UUID appInstanceId = hubWebAppConnection.resolve(HubWebAppWebApi.class).deployAppCommand(
                new UICommandDeployAppArgs(
                        appInstanceName,
                        hackathonAppTemplateId,
                        new UIApplicationPolicyInfo("business-critical", Collections.emptyMap()),
                        new UIApplicationPolicyInfo("single-site", Collections.emptyMap()),
                        siteId,
                        new UIAppTemplateDeploymentPlan(
                                appTemplateConfigurationBySite
                                        .getAppServiceConfigurations()
                                        .stream()
                                        .collect(Collectors.toMap(
                                                UIAppServiceConfiguration::getAppServiceName,
                                                as -> new UIAppServiceDeploymentPlan(
                                                        true,
                                                        space,
                                                        as.getSupportedVersions().keySet().iterator().next(),
                                                        as
                                                                .getSupportedVersions()
                                                                .values()
                                                                .iterator()
                                                                .next()
                                                                .iterator()
                                                                .next())
                                        )),
                                appTemplateConfigurationBySite
                                        .getDataServiceConfigurations()
                                        .stream()
                                        .collect(Collectors.toMap(
                                                UIDataServiceConfiguration::getDataServiceName,
                                                ds -> {
                                                    final Map.Entry<String, UIDSBConfiguration> dsbPlans =
                                                            ds.getDsbPlans().entrySet().iterator().next();
                                                    final Optional<UIDSBPlanConfiguration> aDefault = dsbPlans
                                                            .getValue()
                                                            .getPlans()
                                                            .stream()
                                                            .filter(plan -> plan.getId().equals("default"))
                                                            .findFirst();

                                                    final UIDSBPlanConfiguration plan =
                                                            aDefault.orElseGet(() ->
                                                                    dsbPlans
                                                                            .getValue()
                                                                            .getPlans()
                                                                            .iterator()
                                                                            .next());

                                                    return new UIDataServiceDeploymentPlan(
                                                            dsbPlans.getKey(),
                                                            true,
                                                            plan.getId(),
                                                            plan.getProtocols().iterator().next());
                                                }
                                        ))
                        )
                ));

        // Waiting for app to be deployed
        UIAppInstanceState appInstanceState =
                hubWebAppConnection.resolve(HubWebAppWebApi.class).getAppInstanceState(appInstanceId);
        int i = 0;
        while (appInstanceState.getState() == UIAppInstanceState.StateEnum.DEPLOYING) {
            ++i;
            sleepNoException(3000L);
            appInstanceState = hubWebAppConnection.resolve(HubWebAppWebApi.class).getAppInstanceState(appInstanceId);
            if (i > 10) {
                throw new IllegalStateException("Failed deploying demo app after 10 retries");
            }
        }
        if (appInstanceState.getState() != UIAppInstanceState.StateEnum.RUNNING) {
            throw new IllegalStateException("Failed deploying demo app, resulted in " + appInstanceState.getState());
        }
        return appInstanceId;
    }

    private void sleepNoException(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class ApiInvoker {
        private static Logger log = LoggerFactory.getLogger(DemoHelper.ApiInvoker.class);
        private final ResourceProvider resourceProvider;
        private final WebAPIResolver webAPIResolver;

        public ApiInvoker(ResourceProvider resourceProvider) {
            this.resourceProvider = resourceProvider;
            this.webAPIResolver = getResolver();
        }

        protected WebAPIResolver getResolver() {
            return new DefaultWebApiResolver().buildResolver(
                    new WebApiResolverBuilder()
                            .withBasicAuthentication("shpandrak", "1234")
                            .withVerifySsl(false));
        }

        private HubWebAppWebApi getHubWebAppWebApi() {
            return proxy("hub-web", HubWebAppWebApi.class);
        }

        public void createAppTemplate(UICommandCreateAppTemplate createAppTemplate) {
            createAppTemplate(createAppTemplate, null);
        }

        /**
         * Creates an application template on the hub
         */
        public void createAppTemplate(
                UICommandCreateAppTemplate createAppTemplate,
                InputStream appTemplateImageStream) {
            final List<UIApplicationTemplate> allTemplates = proxy("hub-web", HubWebAppWebApi.class).listAppTemplates();
            if (allTemplates.stream().anyMatch(t -> t.getName().equals(createAppTemplate.getName()))) {
                log.info("App template for " + createAppTemplate.getName() + " already exist, skipping creation");
            } else {
                UUID appInstanceId = proxy("hub-web", HubWebAppWebApi.class).createAppTemplate(createAppTemplate);
                if (appTemplateImageStream != null) {
                    uploadAppTemplateIcon(appInstanceId, appTemplateImageStream);
                }
            }
        }

        @NoJavadoc
        // TODO add javadoc
        public void addJiraIntegration(String jiraUrl, String jiraProjectId, String jiraIssueTypeId) {
            getHubWebAppWebApi().addJiraIntegration(
                    new UICommandAddJiraIntegration(
                            jiraUrl,
                            jiraProjectId,
                            jiraIssueTypeId
                    )
            );
        }

        public Collection<UIUser> listUsers() {
            return proxy("hub-web", HubWebAppUserWebApi.class).listUsers();
        }

        private void uploadAppTemplateIcon(UUID appTemplateId, InputStream appTemplateImageStream) {
            proxy("hub-web", HubWebAppImageStoreWebApi.class)
                    .uploadIcon(appTemplateImageStream, "app-template", appTemplateId.toString());
        }

        @NoJavadoc
        // TODO add javadoc
        public void addSiteToHub(String siteUrn, String siteUrl) {
            if (proxy("hub", HubWebApi.class)
                    .listSites()
                    .stream()
                    .anyMatch(siteDTO -> siteDTO.getUrn().equals(siteUrn))) {
                log.info("Site " + siteUrn + " is already registered on hub. skipping registration");
            } else {
                proxy("hub", HubWebApi.class).addSite(new AddSiteToHubCommandArgs(siteUrn, siteUrl));
            }
        }

        public void addPsb(String siteUrl, String psbUrn, String psbUrl) {
            proxyViaUrl(siteUrl, SiteWebApi.class).registerPsb(new RegisterPsbToSiteCommandArgs(psbUrn, psbUrl));
        }

        public void addMavenArtifactRegistry(String siteUrl, String name, String url) {
            proxyViaUrl(siteUrl, SiteWebApi.class).addMavenArtifactRegistry(
                    new AddMavenArtifactRegistryToSiteCommandArgs(name, url, null, null));
        }

        public void addCustomRestArtifactRegistry(String siteUrl, String name, String url) {
            proxyViaUrl(siteUrl, SiteWebApi.class).addCustomRestArtifactRegistry(
                    new AddCustomArtifactRegistryToSiteCommandArgs(name, url));
        }

        public void addDsb(String siteUrl, String dsbUrn, String dsbUrl) {
            proxyViaUrl(siteUrl, SiteWebApi.class).registerDsb(new RegisterDsbToSiteCommandArgs(dsbUrn, dsbUrl));
        }

        public void addCrb(String siteUrl, String crbUrn, String crbUrl) {
            proxyViaUrl(siteUrl, SiteWebApi.class).registerCrb(new RegisterCrbToSiteCommandArgs(crbUrn, crbUrl));
        }

        public <T> T proxy(String urn, Class<T> resourceWebAPI) {
            String route = resourceProvider.getServiceRegistryApi().getServiceConfig(urn).getRoute();
            return webAPIResolver.getWebAPI(route, resourceWebAPI);
        }

        public <T> T proxyViaUrl(String url, Class<T> resourceWebAPI) {
            return webAPIResolver.getWebAPI(url, resourceWebAPI);
        }

    }
}
