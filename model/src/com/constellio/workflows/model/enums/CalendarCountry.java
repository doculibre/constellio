package com.constellio.workflows.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum CalendarCountry implements EnumWithSmallCode {

	CAQC("ca-qc"), AE("ae");

	private final String code;

	CalendarCountry(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}

	public String getCountry() {
		return code.split("-")[0];
	}

	public String getState() {
		return code.contains("-") ? code.split("-")[1] : "";
	}

	public String[] getCountryAndState() {
		return code.split("-");
	}
}
