package com.constellio.app.extensions.api;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;

public class UserCredentialExtension {
	public ExtensionBooleanResult isEditActionPossible(
			UserCredentialExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isGenerateTokenActionPossible(
			UserCredentialExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public static class UserCredentialExtensionActionPossibleParams {
		private UserCredential userCredential;
		private User user;

		public UserCredentialExtensionActionPossibleParams(UserCredential userCredential, User user) {
			this.userCredential = userCredential;
			this.user = user;
		}

		public Record getRecord() {
			return userCredential.getWrappedRecord();
		}

		public UserCredential getUserCredential() {
			return userCredential;
		}

		public User getUser() {
			return user;
		}
	}
}
