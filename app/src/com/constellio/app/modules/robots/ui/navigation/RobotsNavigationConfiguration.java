package com.constellio.app.modules.robots.ui.navigation;

import static com.constellio.app.ui.framework.components.ComponentState.visibleIf;

import java.io.Serializable;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.modules.robots.constants.RobotsPermissionsTo;
import com.constellio.app.modules.robots.ui.pages.AddEditRobotViewImpl;
import com.constellio.app.modules.robots.ui.pages.ListRootRobotsViewImpl;
import com.constellio.app.modules.robots.ui.pages.RobotConfigurationViewImpl;
import com.constellio.app.modules.robots.ui.pages.RobotLogsViewImpl;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.management.AdminView;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public class RobotsNavigationConfiguration implements Serializable {

	public static final String ROBOTS = "robots";
	public static final String ROBOTS_ICON = "images/icons/config/robot_platform_truck.png";
    public static final String ADD_EDIT_ROBOT = "addEditRobot";
    public static final String LIST_ROOT_ROBOTS = "listRootRobots";
    public static final String ROBOT_CONFIGURATION = "robotConfiguration";
    public static final String ROBOT_LOGS = "robotLogs";

    public void configureNavigation(NavigationConfig config) {
		configureCollectionAdmin(config);
	}

    public static void configureNavigation(NavigatorConfigurationService service) {
        service.register(ADD_EDIT_ROBOT, AddEditRobotViewImpl.class);
        service.register(LIST_ROOT_ROBOTS, ListRootRobotsViewImpl.class);
        service.register(ROBOT_CONFIGURATION, RobotConfigurationViewImpl.class);
        service.register(ROBOT_LOGS, RobotLogsViewImpl.class);
    }

    private void configureCollectionAdmin(NavigationConfig config) {
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(ROBOTS, ROBOTS_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to(RobotViews.class).listRootRobots();
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return visibleIf(user.has(RobotsPermissionsTo.MANAGE_ROBOTS).globally());
			}
		});
	}

}
