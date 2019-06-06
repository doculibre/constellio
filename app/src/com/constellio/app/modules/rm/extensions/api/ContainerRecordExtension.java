package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class ContainerRecordExtension {

	public ExtensionBooleanResult isAddToCartActionPossible(ContainerRecordExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isEditActionPossible(ContainerRecordExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isSlipActionPossible(ContainerRecordExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isLabelActionPossible(ContainerRecordExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isDeleteActionPossible(ContainerRecordExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isEmptyTheBoxActionPossible(ContainerRecordExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public static class ContainerRecordExtensionActionPossibleParams {
		private ContainerRecord containerRecord;
		private User user;

		public ContainerRecordExtensionActionPossibleParams(ContainerRecord containerRecord, User user) {
			this.containerRecord = containerRecord;
			this.user = user;
		}

		public Record getRecord() {
			return containerRecord.getWrappedRecord();
		}

		public ContainerRecord getFolder() {
			return containerRecord;
		}

		public User getUser() {
			return user;
		}
	}
}
