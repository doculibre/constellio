package com.constellio.app.modules.rm.extensions;

public abstract class ExternalLinkServicesExtension {
	public abstract void beforeExternalLinkImport(BeforeExternalLinkImportParams params);

	public abstract void importExternalLink(ImportExternalLinkParams params) throws Exception;

	public static class BeforeExternalLinkImportParams {
		private String username;

		public BeforeExternalLinkImportParams(String username) {
			this.username = username;
		}

		public String getUsername() {
			return username;
		}
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
