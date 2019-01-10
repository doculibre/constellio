package com.constellio.model.entities.schemas.entries;

import com.constellio.model.entities.EnumWithSmallCode;

public enum AggregationType implements EnumWithSmallCode {

	SUM("SUM"), REFERENCE_COUNT("RC"), MIN("MIN"), MAX("MAX"), CALCULATED("CAL"), VALUES_UNION("UNION"),
	LOGICAL_AND("AND"), LOGICAL_OR("OR");

	private String code;

	AggregationType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
