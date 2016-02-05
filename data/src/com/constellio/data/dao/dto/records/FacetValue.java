package com.constellio.data.dao.dto.records;

import java.io.Serializable;

public class FacetValue implements Serializable {
	private final String value;
	private final long quantity;

	public FacetValue(String value, long quantity) {
		this.value = value;
		this.quantity = quantity;
	}

	public String getValue() {
		return value;
	}

	public long getQuantity() {
		return quantity;
	}
}
