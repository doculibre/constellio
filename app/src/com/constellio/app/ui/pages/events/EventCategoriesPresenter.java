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
package com.constellio.app.ui.pages.events;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.extensions.ConstellioPluginManagerRuntimeException.ConstellioPluginManagerRuntimeException_NoSuchModule;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;

public class EventCategoriesPresenter extends BasePresenter<EventCategoriesView> {

	public EventCategoriesPresenter(EventCategoriesView view) {
		super(view);
	}

	void viewEntered() {
		ModelLayerFactory modelLayerFactory = view.getConstellioFactories().getModelLayerFactory();
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);
		boolean agentEnabled = rmConfigs.isAgentEnabled();
		view.setAgentEventsVisible(agentEnabled);
	}

	public void eventButtonClicked(EventCategory eventCategory) {
		if (eventCategory == EventCategory.AGENT_EVENTS) {
			view.navigateTo().listAgentLogs();
		} else if (eventCategory == EventCategory.TASKS_EVENTS) {
			view.navigateTo().listTasksLogs();
		} else {
			view.navigateTo().showEventCategory(eventCategory);
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.VIEW_EVENTS).globally();
	}

	public boolean isTaskModuleInstalled() {
		return false;
		/*ConstellioModulesManager modulesManager = appLayerFactory.getModulesManager();
		try {
			Module tasksModule = modulesManager.getInstalledModule(TaskModule.ID);
			return modulesManager.isModuleEnabled(collection, tasksModule);
		} catch (ConstellioPluginManagerRuntimeException_NoSuchModule e) {
			return false;
		}*/
	}
}
