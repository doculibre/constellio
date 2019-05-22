package com.constellio.app.services.sip.mets;

public class MetsEADMetadataReference extends MetsDivisionInfo {

	private final String path;

	public MetsEADMetadataReference(String id, String parentId, String type, String label, String path) {
		super(id, parentId, label, type);
		this.path = path;
	}

	public String getPath() {
		return path;
	}
}
