package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum RetentionRuleTransaction implements EnumWithSmallCode {
	ADD("add"), MODIFY("modify"), DELETE("delete");

	private final String code;

	RetentionRuleTransaction(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}


	public static RetentionRuleTransaction getTransaction(String code) {
		if (code == null) {
			return null;
		}

		for (RetentionRuleTransaction e : values()) {
			if (e.code.equals(code)) {
				return e;
			}
		}

		return null;
	}
}