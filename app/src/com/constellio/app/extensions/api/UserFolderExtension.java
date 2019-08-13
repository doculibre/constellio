package com.constellio.app.extensions.api;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserFolder;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class UserFolderExtension {

	public ExtensionBooleanResult isFileActionPossible(
			UserFolderExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	@AllArgsConstructor
	@Getter
	public static class UserFolderExtensionActionPossibleParams {
		private UserFolder userFolder;
		private User user;

		public Record getRecord() {
			return userFolder.getWrappedRecord();
		}
	}
}
