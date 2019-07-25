package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public abstract class DocumentExtension {

	public ExtensionBooleanResult isDisplayActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isOpenActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isEditActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isDownloadActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isCopyActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isCreateSipActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isSendEmailActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isCreatePDFAActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isAddSelectionActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isUploadActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isAddCartActionPossibleOnDocument(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isFinalizeActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isMoveActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isPrintLabelActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isPublishActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isUnPublishActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isShareActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isDeleteActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isAddAuthorizationActionPossible(
			DocumentExtensionActionPossibleParams documentExtensionActionPossibleParams) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isGenerateReportActionPossible(
			DocumentExtensionActionPossibleParams documentExtensionActionPossibleParams) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public static class DocumentExtensionActionPossibleParams {
		private Document document;
		private User user;

		public DocumentExtensionActionPossibleParams(Document document, User user) {
			this.document = document;
			this.user = user;
		}

		public Record getRecord() {
			return document.getWrappedRecord();
		}

		public Document getDocument() {
			return document;
		}

		public User getUser() {
			return user;
		}
	}

}
