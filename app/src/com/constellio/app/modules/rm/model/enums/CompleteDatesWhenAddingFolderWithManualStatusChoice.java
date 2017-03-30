package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum CompleteDatesWhenAddingFolderWithManualStatusChoice implements EnumWithSmallCode {
	ENABLED("E"), DISABLED("D");

	private final String code;

	CompleteDatesWhenAddingFolderWithManualStatusChoice(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}

}
