package com.constellio.app.modules.robots.ui.navigation;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.vaadin.navigator.Navigator;

public class RobotViews extends CoreViews {
	public RobotViews(Navigator navigator) {
		super(navigator);
	}

	// ROBOTS

	public void listRootRobots() {
		navigator.navigateTo(NavigatorConfigurationService.LIST_ROOT_ROBOTS);
	}

	public void robotConfiguration(String rootRobotId) {
		navigator.navigateTo(NavigatorConfigurationService.ROBOT_CONFIGURATION + "/" + rootRobotId);
	}
}
