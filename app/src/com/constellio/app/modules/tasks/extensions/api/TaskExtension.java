package com.constellio.app.modules.tasks.extensions.api;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class TaskExtension {

	public ExtensionBooleanResult isConsultLinkActionPossible(TaskExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isConsultActionPossible(TaskExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isEditActionPossible(TaskExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isAutoAssignActionPossible(TaskExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isCompleteTaskActionPossible(TaskExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isCloseTaskActionPossible(TaskExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isCreateSubTaskActionPossible(TaskExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isDeleteActionPossible(TaskExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isGenerateReportActionPossible(TaskExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}


	@AllArgsConstructor
	@Getter
	public static class TaskExtensionActionPossibleParams {
		private Task containerRecord;
		private User user;

		public Record getRecord() {
			return containerRecord.getWrappedRecord();
		}
	}
}
