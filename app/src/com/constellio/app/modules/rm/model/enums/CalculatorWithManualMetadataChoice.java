package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum CalculatorWithManualMetadataChoice implements EnumWithSmallCode {
	ENABLE("E"), ENABLE_DURING_IMPORT("EI"),	DISABLE("D");

	private final String code;

	CalculatorWithManualMetadataChoice(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}
}
