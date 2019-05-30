package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.server.Resource;

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

	public ExtensionBooleanResult isCreatePDFAActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isFinalizeActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isMoveActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isPublishActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isShareActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public void addMenuItems(DocumentExtensionAddMenuItemParams params) {
	}

	public static abstract class DocumentExtensionAddMenuItemParams {
		public abstract Document getDocument();

		public abstract RecordVO getRecordVO();

		public abstract BaseViewImpl getView();

		public abstract User getUser();

		public abstract void registerMenuItem(String caption, Resource icon, Runnable runnable);
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
