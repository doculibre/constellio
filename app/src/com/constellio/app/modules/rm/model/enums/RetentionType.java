package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum RetentionType implements EnumWithSmallCode {

	OPEN("O"), UNTIL_REPLACED("R"), FIXED("F");

	private final String code;

	RetentionType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
