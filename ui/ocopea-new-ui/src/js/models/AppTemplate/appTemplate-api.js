const appTemplateApi = {
  appTemplates:   `${APISERVER}/hub-web-api/app-template`,
  appTemplate:    id => `${APISERVER}/hub-web-api/app-template/${id}`,
  deploy:         `${APISERVER}/hub-web-api/commands/deploy-app`
}

export default appTemplateApi;
