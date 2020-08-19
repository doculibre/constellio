package com.constellio.app.extensions.api;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.users.SystemWideUserInfos;

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
		private SystemWideUserInfos userCredential;
		private User user;

		public UserCredentialExtensionActionPossibleParams(SystemWideUserInfos systemWideUserInfos, User user) {
			this.userCredential = systemWideUserInfos;
			this.user = user;
		}

		public SystemWideUserInfos getUserCredential() {
			return userCredential;
		}

		public User getUser() {
			return user;
		}
	}
}
