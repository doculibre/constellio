package com.constellio.model.entities.schemas.entries;

import com.constellio.model.entities.EnumWithSmallCode;

public enum AggregationType implements EnumWithSmallCode {

	SUM("SUM"), REFERENCE_COUNT("RC");

	private String code;

	AggregationType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
