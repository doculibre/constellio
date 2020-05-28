package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class StorageSpaceExtension {

	public ExtensionBooleanResult isGenerateReportActionPossible(
			StorageSpaceExtensionActionPossibleParams storageSpaceExtensionActionPossibleParams) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isConsultActionPossible(
			StorageSpaceExtensionActionPossibleParams storageSpaceExtensionActionPossibleParams) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isEditActionPossible(
			StorageSpaceExtensionActionPossibleParams storageSpaceExtensionActionPossibleParams) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isDeleteActionPossible(
			StorageSpaceExtensionActionPossibleParams storageSpaceExtensionActionPossibleParams) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isConsultLinkActionPossible(
			StorageSpaceExtensionActionPossibleParams storageSpaceExtensionActionPossibleParams) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public static class StorageSpaceExtensionActionPossibleParams {
		private StorageSpace storageSpace;
		private User user;

		public StorageSpaceExtensionActionPossibleParams(StorageSpace storageSpace, User user) {
			this.storageSpace = storageSpace;
			this.user = user;
		}

		public Record getRecord() {
			return storageSpace.getWrappedRecord();
		}

		public StorageSpace getStorageSpace() {
			return storageSpace;
		}

		public User getUser() {
			return user;
		}
	}
}
