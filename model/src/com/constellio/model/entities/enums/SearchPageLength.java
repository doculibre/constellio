package com.constellio.model.entities.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum SearchPageLength implements EnumWithSmallCode{
	TEN(10), TWENTY_FIVE(25), FIFTY(50), HUNDRED(100);

	private final int value;

	private SearchPageLength(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override
	public String getCode() {
		return ""+value;
	}
}
