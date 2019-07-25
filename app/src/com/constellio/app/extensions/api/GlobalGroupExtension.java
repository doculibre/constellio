package com.constellio.app.extensions.api;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroup;

public class GlobalGroupExtension {
	public ExtensionBooleanResult isAddSubGroupActionPossible(GlobalGroupExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isEditActionPossible(GlobalGroupExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isDeleteActionPossible(GlobalGroupExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public static class GlobalGroupExtensionActionPossibleParams {
		private GlobalGroup globalGroup;
		private User user;

		public GlobalGroupExtensionActionPossibleParams(GlobalGroup globalGroup, User user) {
			this.globalGroup = globalGroup;
			this.user = user;
		}

		public Record getRecord() {
			return globalGroup.getWrappedRecord();
		}

		public GlobalGroup getGlobalGroup() {
			return globalGroup;
		}

		public User getUser() {
			return user;
		}
	}
}
