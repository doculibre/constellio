package com.constellio.model.entities.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum DecryptionVersion implements EnumWithSmallCode {

	VERSION1("version1"), VERSION2("version2");

	private final String code;

	DecryptionVersion(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}
}
