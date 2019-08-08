package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public abstract class FolderExtension {

	public ExtensionBooleanResult isAddDocumentActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isCopyActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isDownloadActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isCreateSipActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isMoveActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isShareActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isDecommissioningActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isBorrowingActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isReturnActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isPrintLabelActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isGenerateReportActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isAddSubFolderActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isDisplayActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isEditActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isDeleteActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isAddAuthorizationActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isAddToCartActionPossible(FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isCreateDecommissioningListActionPossible(
			FolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public static class FolderExtensionActionPossibleParams {
		private Folder folder;
		private User user;

		public FolderExtensionActionPossibleParams(Folder folder, User user) {
			this.folder = folder;
			this.user = user;
		}

		public Record getRecord() {
			return folder.getWrappedRecord();
		}

		public Folder getFolder() {
			return folder;
		}

		public User getUser() {
			return user;
		}
	}

}
