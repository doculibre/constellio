package com.constellio.app.modules.tasks;

import java.io.Serializable;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.modules.tasks.ui.pages.viewGroups.TasksViewGroup;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.base.MainLayout;
import com.constellio.app.ui.pages.home.HomeView;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public class TasksNavigationConfiguration implements Serializable {
	private static final String TASK_MANAGEMENT = "taskManagement";
	public static final String ADD_TASK = "addTask";

	public void configureNavigation(NavigationConfig config) {
		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION2,
				new NavigationItem.Active(TASK_MANAGEMENT, TasksViewGroup.class) {
					@Override
					public void activate(ConstellioNavigator navigateTo) {
						navigateTo.tasksManagement();
					}

					@Override
					public int getOrderValue() {
						return 30;
					}

					@Override
					public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
						return ComponentState.ENABLED;
					}
				});
		config.add(HomeView.ACTION_MENU, new NavigationItem.Active(ADD_TASK) {
			@Override
			public void activate(ConstellioNavigator navigateTo) {
				navigateTo.addTask(null);
			}

			@Override
			public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
				return ComponentState.ENABLED;
			}
		});
	}
}
