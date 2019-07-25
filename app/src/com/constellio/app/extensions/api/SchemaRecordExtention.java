package com.constellio.app.extensions.api;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class SchemaRecordExtention {

	public ExtensionBooleanResult isEditActionPossible(
			SchemaRecordExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isDeleteActionPossible(SchemaRecordExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public static class SchemaRecordExtensionActionPossibleParams {
		private Record record;
		private User user;

		public SchemaRecordExtensionActionPossibleParams(Record record, User user) {
			this.record = record;
			this.user = user;
		}

		public Record getRecord() {
			return record;
		}


		public User getUser() {
			return user;
		}
	}
}
