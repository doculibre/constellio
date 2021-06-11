package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.EnumWithSmallCode;

public enum MessageBodyType implements EnumWithSmallCode {
	HTML("HTML"), PLAIN_TEXT("TXT");

	private String code;

	MessageBodyType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
