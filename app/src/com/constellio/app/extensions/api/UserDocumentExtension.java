package com.constellio.app.extensions.api;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class UserDocumentExtension {

	public ExtensionBooleanResult isFileActionPossible(
			UserDocumentExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	@AllArgsConstructor
	@Getter
	public static class UserDocumentExtensionActionPossibleParams {
		private UserDocument userDocument;
		private User user;

		public Record getRecord() {
			return userDocument.getWrappedRecord();
		}
	}
}
