package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum DecomListStatus implements EnumWithSmallCode {

	GENERATED("G"), PROCESSED("P"), VALIDATED("V"), IN_VALIDATION("IV"), APPROVED("A"), IN_APPROVAL("IA");

	private final String code;

	DecomListStatus(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
