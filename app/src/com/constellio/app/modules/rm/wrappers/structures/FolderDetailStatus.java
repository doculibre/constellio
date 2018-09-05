package com.constellio.app.modules.rm.wrappers.structures;

public enum FolderDetailStatus {
	INCLUDED("included"), EXCLUDED("excluded"), SELECTED("selected");

	private String code;

	FolderDetailStatus(String code) {
		this.code = code;
	}

	public String getDescription() {
		return code;
	}
}
