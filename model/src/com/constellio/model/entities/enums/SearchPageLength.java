package com.constellio.model.entities.enums;

public enum SearchPageLength {
	TEN(10), TWENTY_FIVE(25), FIFTY(50), HUNDRED(100);

	private final int value;

	private SearchPageLength(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
