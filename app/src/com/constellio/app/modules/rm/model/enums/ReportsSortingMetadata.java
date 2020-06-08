package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum ReportsSortingMetadata implements EnumWithSmallCode {

	CODE("C"),
	TITLE("T");

	private final String code;

	ReportsSortingMetadata(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
