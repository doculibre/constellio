package com.constellio.app.ui.pages.imports;

import com.constellio.model.entities.EnumWithSmallCode;

public enum ImportFileMode implements EnumWithSmallCode {

	STRICT("S"), PERMISSIVE("P");

	String code;

	private ImportFileMode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
