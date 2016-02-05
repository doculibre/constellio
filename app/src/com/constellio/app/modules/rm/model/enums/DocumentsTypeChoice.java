package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum DocumentsTypeChoice implements EnumWithSmallCode {

	FORCE_LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES("A"),
	LIMIT_TO_SAME_DOCUMENTS_TYPES_OF_RETENTION_RULES("B"),
	ALL_DOCUMENTS_TYPES("C");

	private final String code;

	DocumentsTypeChoice(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
