package com.constellio.app.modules.rm.extensions;

public abstract class ExternalLinkServicesExtension {
	public abstract void prepareForImport(String username);

	public abstract void importExternalLink(String externalLinkId, String folderId) throws Exception;
}
