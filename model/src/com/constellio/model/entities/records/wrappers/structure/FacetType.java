package com.constellio.model.entities.records.wrappers.structure;

import com.constellio.model.entities.EnumWithSmallCode;

public enum FacetType implements EnumWithSmallCode {
	FIELD("F"), QUERY("Q");

	private final String code;

	FacetType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
