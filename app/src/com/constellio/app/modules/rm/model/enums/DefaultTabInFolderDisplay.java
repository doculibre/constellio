package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum DefaultTabInFolderDisplay implements EnumWithSmallCode {

	SUB_FOLDERS("SF"),
	DOCUMENTS("D"),
	METADATA("M");

	private final String code;

	DefaultTabInFolderDisplay(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
