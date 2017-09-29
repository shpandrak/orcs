import Reflux from 'reflux';

var DeployingProgressActions = Reflux.createActions([
  "setAllLogsFilters",
  "toggleLogsFilter",
  "receiveLog",
  "seeAllLogsFilters",
  "setAllFilters",
  "addFilter",
  "userSearchLogs",
  "clearLogs",
  "initializeSelectedInstance"
]);

export default DeployingProgressActions;
