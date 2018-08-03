package com.constellio.app.modules.robots.ui.navigation;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.modules.robots.constants.RobotsPermissionsTo;
import com.constellio.app.modules.robots.ui.pages.*;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.management.AdminView;
import com.constellio.model.entities.records.wrappers.User;

import java.io.Serializable;

import static com.constellio.app.ui.framework.components.ComponentState.visibleIf;

public class RobotsNavigationConfiguration implements Serializable {

	public static final String ROBOTS = "robots";
	public static final String ROBOTS_ICON = "images/icons/config/robot_platform_truck.png";
	public static final String ADD_EDIT_ROBOT = "addEditRobot";
	public static final String LIST_ROOT_ROBOTS = "listRootRobots";
	public static final String ROBOT_CONFIGURATION = "robotConfiguration";
	public static final String ROBOT_LOGS = "robotLogs";
	public static final String DELETE_ROBOT_RECORDS = "deleteRobotRecords";

	public static void configureNavigation(NavigationConfig config) {
		configureCollectionAdmin(config);
	}

	public static void configureNavigation(NavigatorConfigurationService service) {
		service.register(ADD_EDIT_ROBOT, AddEditRobotViewImpl.class);
		service.register(LIST_ROOT_ROBOTS, ListRootRobotsViewImpl.class);
		service.register(ROBOT_CONFIGURATION, RobotConfigurationViewImpl.class);
		service.register(ROBOT_LOGS, RobotLogsViewImpl.class);
		service.register(DELETE_ROBOT_RECORDS, DeleteRobotRecordsViewImpl.class);
	}

	private static void configureCollectionAdmin(NavigationConfig config) {
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(ROBOTS, ROBOTS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to(RobotViews.class).listRootRobots();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return visibleIf(user.has(RobotsPermissionsTo.MANAGE_ROBOTS).globally());
			}
		});
	}

}
