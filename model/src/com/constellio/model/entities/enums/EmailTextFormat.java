package com.constellio.model.entities.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum EmailTextFormat implements EnumWithSmallCode {

	PLAIN_TEXT("PT"),
	HTML("H");

	private final String code;


	EmailTextFormat(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}

	public boolean isHtml() {
		return this == HTML;
	}

	public boolean isPlainText() {
		return this == PLAIN_TEXT;
	}
}