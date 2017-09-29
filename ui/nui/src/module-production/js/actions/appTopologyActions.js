import Reflux from 'reflux';

var Actions = Reflux.createActions([

    "userClickOnAppTopologyService",
    "userClickOnAppTopologyDependency",
	  "setDependenciesContainerTransition",
    "setServicesContainerTransition",
    "setDependenciesTranslate",
    "setServicesTranslate",
    "initialAppTopology",
    "setSelectedApp",
    "setSelectedElementActiveState",
    "setSelectedElementVersion",
    "setSelectedElementPlan",
    "updateStateIndicators",
    "initializeSelectedElement"

]);

export default Actions;
