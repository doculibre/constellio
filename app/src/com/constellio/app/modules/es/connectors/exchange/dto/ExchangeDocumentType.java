package com.constellio.app.modules.es.connectors.exchange.dto;

import com.constellio.model.entities.EnumWithSmallCode;

public enum ExchangeDocumentType implements EnumWithSmallCode {
	EMAIL("email"), TASK("task"), CALENDAR("calendar"), CONTACT("contact"), BASE("base");
	private final String code;

	ExchangeDocumentType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}