package com.constellio.model.entities.security.global;

import com.constellio.model.entities.EnumWithSmallCode;

public enum UserCredentialStatus implements EnumWithSmallCode {

	ACTIVE("a"), PENDING("p"), SUPENDED("s"), DELETED("d");

	private String code;

	UserCredentialStatus(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
