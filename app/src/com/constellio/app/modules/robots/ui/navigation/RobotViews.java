package com.constellio.app.modules.robots.ui.navigation;

import static com.constellio.app.ui.params.ParamUtils.addParams;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.ui.application.CoreViews;
import com.vaadin.navigator.Navigator;

public class RobotViews extends CoreViews {
	public RobotViews(Navigator navigator) {
		super(navigator);
	}

	// ROBOTS

	public void listRootRobots() {
		navigator.navigateTo(RobotsNavigationConfiguration.LIST_ROOT_ROBOTS);
	}

	public void robotConfiguration(String rootRobotId) {
		navigator.navigateTo(RobotsNavigationConfiguration.ROBOT_CONFIGURATION + "/" + rootRobotId);
	}

	public void addRobot(String parentId) {
		Map<String, String> params = new HashMap<>();
		params.put("pageMode", "add");
		if (parentId != null) {
			params.put("parentId", parentId);
		}
		navigator.navigateTo(addParams(RobotsNavigationConfiguration.ADD_EDIT_ROBOT, params));
	}

	public void editRobot(String robotId) {
		Map<String, String> params = new HashMap<>();
		params.put("pageMode", "edit");
		params.put("robotId", robotId);
		navigator.navigateTo(addParams(RobotsNavigationConfiguration.ADD_EDIT_ROBOT, params));
	}

	public void displayLogs(String entityId) {
		navigator.navigateTo(RobotsNavigationConfiguration.ROBOT_LOGS + "/" + entityId);
	}

	public void deleteRobotRecords(String robotId) {
		navigator.navigateTo(RobotsNavigationConfiguration.DELETE_ROBOT_RECORDS + "/" + robotId);
	}

}
