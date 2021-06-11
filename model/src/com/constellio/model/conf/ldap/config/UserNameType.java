package com.constellio.model.conf.ldap.config;

import com.constellio.model.entities.EnumWithSmallCode;

public enum UserNameType implements EnumWithSmallCode {
	MAIL_NICKNAME("M"), EMAIL("E");

	private String code;

	UserNameType(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}
}