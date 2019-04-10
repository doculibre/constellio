package com.constellio.app.modules.tasks.extensions.api;

import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.modules.tasks.extensions.TaskFormExtention;
import com.constellio.app.modules.tasks.extensions.api.params.TaskFormParams;
import com.constellio.app.modules.tasks.extensions.api.params.TaskFormRetValue;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;

public class TaskModuleExtensions implements ModuleExtensions {

	public VaultBehaviorsList<TaskFormExtention> taskFormExtentions = new VaultBehaviorsList<>();
	AppLayerFactory appLayerFactory;

	public TaskModuleExtensions(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	public TaskFormRetValue taskFormExtentions(TaskFormParams taskFormParams) {
		TaskFormRetValue taskFormRetValue = new TaskFormRetValue();

		for (TaskFormExtention taskFormExtention : taskFormExtentions) {
			TaskFormRetValue currentTaskFormRetValue = taskFormExtention.taskBeingSave(taskFormParams);
			if (currentTaskFormRetValue != null) {
				taskFormRetValue.addAll(currentTaskFormRetValue);
			}
		}

		return taskFormRetValue;
	}
}
