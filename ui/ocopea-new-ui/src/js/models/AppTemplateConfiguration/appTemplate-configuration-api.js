const appTemplateConfiguration = {
  configuration: (siteId, appTemplateId) => {
    return `${APISERVER}/hub-web-api/test-dev/site/${siteId}/app-template-configuration/${appTemplateId}`
  }
}

export default appTemplateConfiguration;
