var ProductionNavigationOptions = {
	module: "production",
	wizard: {
		location: "/wizard/",
		appMarket: {
			subLocation: "App Market"
		},
		appTopology: {
			subLocation: "App Topology"
		},
		siteSetup: {
			subLocation: "Site Setup"
		},
		businessSetup: {
			subLocation: "Business Setup"
		},
		summary: {
			subLocation: "Configuration"
		}
	},
	dashboard: {
		location: "/dashboard/",
		subLocation: ""
	},
	main: {
		location: "/main",
		subLocation: ""
	},
	deployingProgress: {
		location: "/deploying-progress",
		subLocation: ""
	}
}

export default ProductionNavigationOptions;
