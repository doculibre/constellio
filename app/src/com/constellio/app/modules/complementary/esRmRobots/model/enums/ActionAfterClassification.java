package com.constellio.app.modules.complementary.esRmRobots.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum ActionAfterClassification implements EnumWithSmallCode {

	DO_NOTHING("n"), EXCLUDE_DOCUMENTS("x"), DELETE_DOCUMENTS_ON_ORIGINAL_SYSTEM("d");

	private String code;

	ActionAfterClassification(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}

	public boolean isConnectorDocumentExcluded() {
		return this == EXCLUDE_DOCUMENTS || this == DELETE_DOCUMENTS_ON_ORIGINAL_SYSTEM;
	}
}
