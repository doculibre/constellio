package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum RetentionRuleScope implements EnumWithSmallCode {

	DOCUMENTS_AND_FOLDER("DF"), DOCUMENTS("D");

	private String code;

	RetentionRuleScope(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
