package com.constellio.app.ui.pages.events;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.configs.SystemConfigurationsManager;
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
			view.navigate().to(RMViews.class).listAgentLogs();
		} else if (eventCategory == EventCategory.TASKS_EVENTS) {
			view.navigate().to(TaskViews.class).listTasksLogs();
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
