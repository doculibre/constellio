package com.constellio.app.entities.support;

import com.constellio.model.entities.EnumWithSmallCode;

public enum SupportPlan implements EnumWithSmallCode {
	// Do not change order, ordinal is important
	NONE("N"), SILVER("S"), GOLD("G"), PLATINUM("P");

	private final String code;

	SupportPlan(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}
}
