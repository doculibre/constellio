package com.constellio.model.entities.records.wrappers.structure;

import com.constellio.model.entities.EnumWithSmallCode;

public enum FacetOrderType implements EnumWithSmallCode {
	RELEVANCE("R"), ALPHABETICAL("A");

	private final String code;

	FacetOrderType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
