package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum OriginStatus implements EnumWithSmallCode {
	ACTIVE("a"), SEMI_ACTIVE("s");

	private String code;

	OriginStatus(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
