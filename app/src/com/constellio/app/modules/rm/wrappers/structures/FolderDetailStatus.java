package com.constellio.app.modules.rm.wrappers.structures;

public enum FolderDetailStatus {
	INCLUDED("i"), EXCLUDED("e"), SELECTED("s");

	private String code;

	FolderDetailStatus(String code) {
		this.code = code;
	}

	public String getDescription() {
		return code;
	}
}
