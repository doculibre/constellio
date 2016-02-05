package com.constellio.model.services.search.query.logical.criteria;

import com.constellio.model.entities.EnumWithSmallCode;

public enum MeasuringUnitTime implements EnumWithSmallCode {

	DAYS("D"), WEEKS("W"), MONTHS("M"), YEARS("Y");

	private String code;

	MeasuringUnitTime(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
