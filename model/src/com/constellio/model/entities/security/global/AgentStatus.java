package com.constellio.model.entities.security.global;

import com.constellio.model.entities.EnumWithSmallCode;

public enum AgentStatus implements EnumWithSmallCode {

	ENABLED("E"), DISABLED("D"), MANUALLY_DISABLED("M");

	private String code;

	AgentStatus(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
