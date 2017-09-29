import Reflux from 'reflux';

var SettingsActions = Reflux.createActions([
  "showIntegrationDialog",
  "hideIntegrationDialog",
  "receiveData",
  "leftMenuNavigation",
  "submitJiraConfig",
  "configureJiraIssueType",
  "configureJiraProject",
  "configureJiraUrl",
	"getIntegrations",
  "receiveJiraProjectsAndIssueTypes",
  "receiveJiraDetails",
  "showJiraCredentialDialog",
  "hideJiraCredentialDialog",
  "clearJiraData",
  "startJiraLoader",
  "stopJiraLoading"
]);

export default SettingsActions;
