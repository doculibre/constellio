/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
