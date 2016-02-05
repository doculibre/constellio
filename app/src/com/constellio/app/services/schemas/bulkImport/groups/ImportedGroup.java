package com.constellio.app.services.schemas.bulkImport.groups;

public class ImportedGroup {

	private String code;
	private String parent;
	private String title;

	public String getCode() {
		return code;
	}

	public ImportedGroup setCode(String code) {
		this.code = code;
		return this;
	}

	public String getParent() {
		return parent;
	}

	public ImportedGroup setParent(String parent) {
		this.parent = parent;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public ImportedGroup setTitle(String title) {
		this.title = title;
		return this;
	}
}
