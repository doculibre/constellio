package com.constellio.app.modules.rm.extensions.api;

import com.constellio.model.entities.records.Record;

public abstract class FolderExtension {

	public boolean isCopyActionPossible(FolderExtensionActionPossibleParams params) {
		return true;
	}

	public boolean isMoveActionPossible(FolderExtensionActionPossibleParams params) {
		return true;
	}

	public boolean isShareActionPossible(FolderExtensionActionPossibleParams params) {
		return true;
	}

	public boolean isDecommissioningActionPossible(FolderExtensionActionPossibleParams params) {
		return true;
	}

	public static class FolderExtensionActionPossibleParams {
		private Record record;

		public FolderExtensionActionPossibleParams(Record record) {
			this.record = record;
		}

		public Record getRecord() {
			return record;
		}
	}

}
