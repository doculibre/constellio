package com.constellio.model.entities.security.global;

import com.constellio.model.entities.EnumWithSmallCode;

public enum UserSyncMode implements EnumWithSmallCode {
	SYNCED("s"), NOT_SYNCED("n"), LOCALLY_CREATED("l");

	private String code;

	UserSyncMode(String code) {
		this.code = code;
	}

	public static UserSyncMode fastConvert(String value) {
		if (value == null) {
			return null;
		}
		switch (value) {
			case "s":
				return UserSyncMode.SYNCED;

			case "n":
				return UserSyncMode.NOT_SYNCED;

			case "l":
				return UserSyncMode.LOCALLY_CREATED;

			default:
				throw new IllegalArgumentException("Unsupported value : " + value);
		}
	}

	public String getCode() {
		return code;
	}
}
