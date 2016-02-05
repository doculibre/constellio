package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum DisposalType implements EnumWithSmallCode {

	SORT("T"), DESTRUCTION("D"), DEPOSIT("C");

	private final String code;

	DisposalType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public boolean isDestructionOrSort() {
		return this == SORT || this == DESTRUCTION;
	}

	public boolean isDepositOrSort() {
		return this == SORT || this == DEPOSIT;
	}

	public static boolean isValidCode(String code) {
		return "T".equals(code) || "D".equals(code) || "C".equals(code);
	}
}
