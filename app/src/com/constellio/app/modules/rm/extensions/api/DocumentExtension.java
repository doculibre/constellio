package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.server.Resource;

public abstract class DocumentExtension {

	public boolean isCopyActionPossible(DocumentExtensionActionPossibleParams params) {
		return true;
	}

	public boolean isCreatePDFAActionPossible(DocumentExtensionActionPossibleParams params) {
		return true;
	}

	public boolean isFinalizeActionPossible(DocumentExtensionActionPossibleParams params) {
		return true;
	}

	public boolean isMoveActionPossible(DocumentExtensionActionPossibleParams params) {
		return true;
	}

	public boolean isPublishActionPossible(DocumentExtensionActionPossibleParams params) {
		return true;
	}

	public boolean isShareActionPossible(DocumentExtensionActionPossibleParams params) {
		return true;
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
		private Record record;

		public DocumentExtensionActionPossibleParams(Record record) {
			this.record = record;
		}

		public Record getRecord() {
			return record;
		}
	}

}
