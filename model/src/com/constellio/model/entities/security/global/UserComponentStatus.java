package com.constellio.model.entities.security.global;

import com.constellio.model.entities.EnumWithSmallCode;

public enum UserComponentStatus implements EnumWithSmallCode {
	ACTIVE("a"), INACTIVE("i");

	private String code;

	UserComponentStatus(String code) {
		this.code = code;
	}

	public static UserComponentStatus fastConvert(String value) {
		if (value == null) {
			return null;
		}
		switch (value) {
			case "a":
				return UserComponentStatus.ACTIVE;

			case "p":
				return UserComponentStatus.INACTIVE;

			default:
				throw new IllegalArgumentException("Unsupported value : " + value);
		}
	}

	public String getCode() {
		return code;
	}
}
