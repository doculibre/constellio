package com.constellio.app.modules.robots.ui.navigation;

import static com.constellio.app.ui.framework.components.ComponentState.visibleIf;

import java.io.Serializable;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.modules.robots.constants.RobotsPermissionsTo;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.management.AdminView;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public class RobotsNavigationConfiguration implements Serializable {

	public static final String ROBOTS = "robots";
	public static final String ROBOTS_ICON = "images/icons/config/robot_platform_truck.png";

	public void configureNavigation(NavigationConfig config) {
		configureCollectionAdmin(config);
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
