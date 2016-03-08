package com.constellio.app.modules.robots.ui.navigation;

import static com.constellio.app.ui.params.ParamUtils.addParams;

import java.util.Map;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.google.gwt.dev.util.collect.HashMap;
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

	public void addRobot(String parentId) {
		Map<String, String> params = new HashMap<>();
		params.put("pageMode", "add");
		if (parentId != null) {
			params.put("parentId", parentId);
		}
		navigator.navigateTo(addParams(NavigatorConfigurationService.ADD_EDIT_ROBOT, params));
	}

	public void editRobot(String robotId) {
		Map<String, String> params = new HashMap<>();
		params.put("pageMode", "edit");
		params.put("robotId", robotId);
		navigator.navigateTo(addParams(NavigatorConfigurationService.ADD_EDIT_ROBOT, params));
	}

	public void displayLogs(String entityId) {
		navigator.navigateTo(NavigatorConfigurationService.ROBOT_LOGS + "/" + entityId);
	}
}
