package com.constellio.model.entities.security.global;

import com.constellio.model.entities.EnumWithSmallCode;

public enum UserCredentialStatus implements EnumWithSmallCode {
	ACTIVE("a"), PENDING("p"), SUSPENDED("s"), DELETED("d");

	private String code;

	UserCredentialStatus(String code) {
		this.code = code;
	}

	public static UserCredentialStatus fastConvert(String value) {
		if (value == null) {
			return null;
		}
		switch (value) {
			case "a":
				return UserCredentialStatus.ACTIVE;

			case "p":
				return UserCredentialStatus.PENDING;

			case "s":
				return UserCredentialStatus.SUSPENDED;

			case "d":
				return UserCredentialStatus.DELETED;

			default:
				throw new IllegalArgumentException("Unsupported value : " + value);
		}
	}

	public String getCode() {
		return code;
	}
}
