package com.constellio.model.entities.security.global;

import com.constellio.model.entities.EnumWithSmallCode;

public enum GlobalGroupStatus implements EnumWithSmallCode {
	ACTIVE("a"), INACTIVE("d");

	private String code;

	GlobalGroupStatus(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
