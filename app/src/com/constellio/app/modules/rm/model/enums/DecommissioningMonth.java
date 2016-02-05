package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum DecommissioningMonth implements EnumWithSmallCode {

	JANUARY("JAN"), FEBRUARY("FEV"), MARCH("MAR"), APRIL("APR"), MAY("MAY"), JUNE("JUN"),
	JULY("JUL"), AUGUST("AUG"), SEPTEMBER("SEP"), OCTOBER("OCT"), NOVEMBER("NOV"), DECEMBER("DEC");

	private final String code;

	DecommissioningMonth(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
