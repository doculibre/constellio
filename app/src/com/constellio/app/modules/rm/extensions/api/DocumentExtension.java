package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.server.Resource;

public abstract class DocumentExtension {

	public ExtensionBooleanResult isCopyActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isCreatePDFAActionPossible(DocumentExtensionActionPossibleParams params) {
		return hasWriteAccess(params) ? ExtensionBooleanResult.NOT_APPLICABLE : ExtensionBooleanResult.FALSE;
	}

	public ExtensionBooleanResult isFinalizeActionPossible(DocumentExtensionActionPossibleParams params) {
		return hasWriteAccess(params) ? ExtensionBooleanResult.NOT_APPLICABLE : ExtensionBooleanResult.FALSE;
	}

	public ExtensionBooleanResult isMoveActionPossible(DocumentExtensionActionPossibleParams params) {
		return hasWriteAccess(params) ? ExtensionBooleanResult.NOT_APPLICABLE : ExtensionBooleanResult.FALSE;
	}

	public ExtensionBooleanResult isPublishActionPossible(DocumentExtensionActionPossibleParams params) {
		return hasWriteAccess(params) ? ExtensionBooleanResult.NOT_APPLICABLE : ExtensionBooleanResult.FALSE;
	}

	public ExtensionBooleanResult isShareActionPossible(DocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	protected boolean hasWriteAccess(DocumentExtensionActionPossibleParams params) {
		Document document = params.getDocument();
		User user = params.getUser();
		return user.hasWriteAccess().on(document);
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
