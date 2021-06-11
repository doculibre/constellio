package com.constellio.app.modules.tasks.extensions.api;

import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.modules.tasks.extensions.TaskEmailExtension;
import com.constellio.app.modules.tasks.extensions.TaskFormExtension;
import com.constellio.app.modules.tasks.extensions.api.TaskExtension.TaskExtensionActionPossibleParams;
import com.constellio.app.modules.tasks.extensions.api.params.TaskFormParams;
import com.constellio.app.modules.tasks.extensions.api.params.TaskFormRetValue;
import com.constellio.app.modules.tasks.extensions.param.HasReferenceWithModifiedRecordParam;
import com.constellio.app.modules.tasks.extensions.ui.TaskDisplayFactoryExtension;
import com.constellio.app.modules.tasks.extensions.ui.TaskDisplayFactoryExtension.TaskDisplayFactoryExtensionParams;
import com.constellio.app.modules.tasks.extensions.ui.TaskTableExtension;
import com.constellio.app.modules.tasks.extensions.ui.TaskTableExtension.TaskTableColumnsExtensionParams;
import com.constellio.app.modules.tasks.extensions.ui.TaskTableExtension.TaskTableComponentsExtensionParams;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.Component;

import java.util.ArrayList;
import java.util.List;

public class TaskModuleExtensions implements ModuleExtensions {

	public VaultBehaviorsList<TaskEmailExtension> taskEmailExtensions = new VaultBehaviorsList<>();
	public VaultBehaviorsList<TaskFormExtension> taskFormExtentions = new VaultBehaviorsList<>();
	public VaultBehaviorsList<TaskExtension> taskExtensions = new VaultBehaviorsList<>();
	public VaultBehaviorsList<TaskDisplayFactoryExtension> taskDisplayFactoryExtensions = new VaultBehaviorsList<>();
	public VaultBehaviorsList<TaskTableExtension> taskTableExtensions = new VaultBehaviorsList<>();

	AppLayerFactory appLayerFactory;

	public TaskModuleExtensions(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	public TaskFormRetValue taskFormExtensions(TaskFormParams taskFormParams) {
		TaskFormRetValue taskFormRetValue = new TaskFormRetValue();

		for (TaskFormExtension taskFormExtension : taskFormExtentions) {
			TaskFormRetValue currentTaskFormRetValue = taskFormExtension.taskBeingSave(taskFormParams);
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

	public Component getTaskDisplayFactory(TaskDisplayFactoryExtensionParams params) {
		for (TaskDisplayFactoryExtension taskDisplayFactoryExtension : taskDisplayFactoryExtensions) {
			Component component = taskDisplayFactoryExtension.getDisplayComponent(params);
			if (component != null) {
				return component;
			}
		}
		return null;
	}

	public void addTaskTableExtraColumns(TaskTableColumnsExtensionParams params) {
		for (TaskTableExtension taskTableExtension : taskTableExtensions) {
			taskTableExtension.addExtraColumns(params);
		}
	}

	public void addTaskTableExtraComponents(TaskTableComponentsExtensionParams params) {
		for (TaskTableExtension taskTableExtension : taskTableExtensions) {
			taskTableExtension.addExtraComponents(params);
		}
	}

	public boolean hasReferenceWithModifiedRecord(HasReferenceWithModifiedRecordParam param) {
		return taskFormExtentions.getBooleanValue(true,
				(behavior) -> behavior.hasReferenceWithModifiedRecord(param));
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

	public boolean isConsultActionPossibleOnTask(final Task task, final User user) {
		return taskExtensions.getBooleanValue(true,
				(behavior -> behavior.isConsultActionPossible(new TaskExtensionActionPossibleParams(task, user))));
	}

	public boolean isConsultLinkActionPossibleOnTask(final Task task, final User user) {
		return taskExtensions.getBooleanValue(true,
				behavior -> behavior.isConsultLinkActionPossible(new TaskExtensionActionPossibleParams(task, user)));
	}
}
