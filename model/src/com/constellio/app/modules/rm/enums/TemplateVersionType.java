package com.constellio.app.modules.rm.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum TemplateVersionType implements EnumWithSmallCode {
	CONSTELLIO_5("c5"), CONSTELLIO_10("c10");

	private final String code;

	TemplateVersionType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
