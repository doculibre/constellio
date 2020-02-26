package com.constellio.app.ui.pages.events;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashMap;

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
		} else if (eventCategory == EventCategory.IMPORT_EXPORT) {
			view.navigate().to(CoreViews.class).listImportExport();
		} else if (eventCategory == EventCategory.CURRENTLY_BORROWED_DOCUMENTS) {
			HashMap<String, Object> params = new HashMap<>();
			params.put(EventViewParameters.EVENT_TYPE, EventType.CURRENT_BORROW_DOCUMENT);
			params.put(EventViewParameters.EVENT_CATEGORY, EventCategory.CURRENTLY_BORROWED_DOCUMENTS);
			view.navigate().to(RMViews.class).showEvent(params);
		} else {
			if (eventCategory == EventCategory.REINDEX_AND_RESTART) {
				File folder = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newFileService()
						.newTemporaryFolder("exportLogs");
				if (folder != null) {
					File logFile = new File(folder, "constellio.log");//D%V bon fichier de log?
					if (logFile != null) {
						FileUtils.deleteQuietly(logFile);
					}
				}
			}
			view.navigate().to().showEventCategory(eventCategory);
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.VIEW_EVENTS).onSomething();
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
