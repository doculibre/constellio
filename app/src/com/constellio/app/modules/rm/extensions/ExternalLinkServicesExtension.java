package com.constellio.app.modules.rm.extensions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public abstract class ExternalLinkServicesExtension {
	public abstract void setupExternalLinkImport(SetupExternalLinkImportParams params);

	public abstract void beforeExternalLinkImport(BeforeExternalLinkImportParams params);

	public abstract String importExternalLink(ImportExternalLinkParams params) throws Exception;

	public static class SetupExternalLinkImportParams {
		private String currentUsersUsername;
		private String requester;

		public SetupExternalLinkImportParams(String currentUsersUsername, String requester) {
			this.currentUsersUsername = currentUsersUsername;
			this.requester = requester;
		}

		public String getCurrentUsersUsername() {
			return currentUsersUsername;
		}

		public String getRequester() {
			return requester;
		}
	}

	@Getter
	@AllArgsConstructor
	public static class BeforeExternalLinkImportParams {
		private List<String> willBeImportedExternalLinks;
	}

	public static class ImportExternalLinkParams {
		private String externalLinkId;
		private String folderId;

		public ImportExternalLinkParams(String externalLinkId, String folderId) {
			this.externalLinkId = externalLinkId;
			this.folderId = folderId;
		}

		public String getExternalLinkId() {
			return externalLinkId;
		}

		public String getFolderId() {
			return folderId;
		}
	}
}
