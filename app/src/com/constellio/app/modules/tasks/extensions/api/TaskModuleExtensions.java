package com.constellio.app.modules.tasks.extensions.api;

import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.modules.tasks.extensions.TaskEmailExtension;
import com.constellio.app.modules.tasks.extensions.TaskFormExtention;
import com.constellio.app.modules.tasks.extensions.api.TaskExtension.TaskExtensionActionPossibleParams;
import com.constellio.app.modules.tasks.extensions.api.params.TaskFormParams;
import com.constellio.app.modules.tasks.extensions.api.params.TaskFormRetValue;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.model.entities.records.wrappers.User;

import java.util.ArrayList;
import java.util.List;

public class TaskModuleExtensions implements ModuleExtensions {

	public VaultBehaviorsList<TaskEmailExtension> taskEmailExtensions = new VaultBehaviorsList<>();
	public VaultBehaviorsList<TaskFormExtention> taskFormExtentions = new VaultBehaviorsList<>();
	public VaultBehaviorsList<TaskExtension> taskExtensions = new VaultBehaviorsList<>();
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

	public List<String> taskEmailParameters(Task task) {
		List<String> parameters = new ArrayList<>();
		for (TaskEmailExtension taskEmailExtension : taskEmailExtensions) {
			parameters.addAll(taskEmailExtension.newParameters(task));
		}
		return parameters;
	}

	public boolean isEditActionPossibleOnTask(final Task task, final User user) {
		return taskExtensions.getBooleanValue(true,
				(behavior) -> behavior.isEditActionPossible(new TaskExtensionActionPossibleParams(task, user)));
	}

	public boolean isAutoAssignActionPossibleOnTask(final Task task, final User user) {
		return taskExtensions.getBooleanValue(true,
				(behavior) -> behavior.isEditActionPossible(new TaskExtensionActionPossibleParams(task, user)));
	}

	public boolean isCompleteTaskActionPossibleOnTask(final Task task, final User user) {
		return taskExtensions.getBooleanValue(true,
				(behavior) -> behavior.isEditActionPossible(new TaskExtensionActionPossibleParams(task, user)));
	}

	public boolean isCloseTaskActionPossibleOnTask(final Task task, final User user) {
		return taskExtensions.getBooleanValue(true,
				(behavior) -> behavior.isEditActionPossible(new TaskExtensionActionPossibleParams(task, user)));
	}

	public boolean isCreateSubTaskActionPossibleOnTask(final Task task, final User user) {
		return taskExtensions.getBooleanValue(true,
				(behavior) -> behavior.isEditActionPossible(new TaskExtensionActionPossibleParams(task, user)));
	}

	public boolean isDeleteActionPossibleOnTask(final Task task, final User user) {
		return taskExtensions.getBooleanValue(true,
				(behavior) -> behavior.isEditActionPossible(new TaskExtensionActionPossibleParams(task, user)));
	}

	public boolean isGenerateReportActionPossibleOnTask(final Task task, final User user) {
		return taskExtensions.getBooleanValue(true,
				(behavior) -> behavior.isEditActionPossible(new TaskExtensionActionPossibleParams(task, user)));
	}
}
