package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;

import java.util.ArrayList;
import java.util.List;

public class TaskModuleExtensions implements ModuleExtensions {
	public VaultBehaviorsList<TaskEmailExtension> taskEmailExtensions = new VaultBehaviorsList<>();
	AppLayerFactory appLayerFactory;

	public TaskModuleExtensions(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	public List<String> taskEmailParameters(Task task) {
		List<String> parameters = new ArrayList<>();
		for (TaskEmailExtension taskEmailExtension : taskEmailExtensions) {
			parameters.addAll(taskEmailExtension.newParameters(task));
		}
		return parameters;
	}
}
