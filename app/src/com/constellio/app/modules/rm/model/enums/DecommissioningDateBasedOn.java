package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum DecommissioningDateBasedOn implements EnumWithSmallCode {

	CLOSE_DATE("C"), OPEN_DATE("O");

	private final String code;

	DecommissioningDateBasedOn(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
