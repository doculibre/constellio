package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum CopyType implements EnumWithSmallCode {
	PRINCIPAL("P"), SECONDARY("S");

	private final String code;

	CopyType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}


	public static CopyType getCopyType(String code) {
		if (code == null) {
			return null;
		}

		for (CopyType e : values()) {
			if (e.code.equals(code)) {
				return e;
			}
		}

		return null;
	}
}
