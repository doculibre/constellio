package com.constellio.model.entities.enums;

public enum ContentCheckoutSource {

	CONSTELLIO(0);

	private final int value;

	ContentCheckoutSource(final int newValue) {
		value = newValue;
	}

	public int getValue() {
		return value;
	}

}
