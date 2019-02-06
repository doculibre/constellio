package com.constellio.app.modules.rm.services.sip.mets;

public class MetsDivisionInfo {

	private final String id;

	private final String parentId;

	private final String label;

	private final String type;

	public MetsDivisionInfo(String id, String parentId, String label, String type) {
		this.id = id;
		this.parentId = parentId;
		this.label = label;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public String getParentId() {
		return parentId;
	}

	public String getLabel() {
		return label;
	}

	public String getType() {
		return type;
	}
}
