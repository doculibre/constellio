package com.constellio.model.entities.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum TableMode implements EnumWithSmallCode {

	LIST("list"), TABLE("table");

	private final String code;

	TableMode(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}

	public static TableMode byCode(String code) {
		for (TableMode tableMode : values()) {
			if (tableMode.getCode().equals(code)) {
				return tableMode;
			}
		}
		throw new IllegalArgumentException(code);
	}
}
