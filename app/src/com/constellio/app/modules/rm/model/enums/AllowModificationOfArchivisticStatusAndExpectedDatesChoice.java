package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum AllowModificationOfArchivisticStatusAndExpectedDatesChoice implements EnumWithSmallCode {
	ENABLED("E"), ENABLED_FOR_IMPORTED_RECORDS("EI"), DISABLED("D");

	private final String code;

	AllowModificationOfArchivisticStatusAndExpectedDatesChoice(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}

	public boolean isAlwaysEnabledOrDuringImportOnly() {
		return this == ENABLED || this == ENABLED_FOR_IMPORTED_RECORDS;
	}
}
