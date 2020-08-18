package com.constellio.model.entities.records.wrappers.structure;

import com.constellio.model.entities.EnumWithSmallCode;

public enum ExternalAccessUrlStatus implements EnumWithSmallCode {
	OPEN("O"), TO_CLOSE("T"), EXPIRED("E"), CLOSED("C");

	private final String code;

	ExternalAccessUrlStatus(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
