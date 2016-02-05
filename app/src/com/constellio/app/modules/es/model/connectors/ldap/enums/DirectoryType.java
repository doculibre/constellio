package com.constellio.app.modules.es.model.connectors.ldap.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum DirectoryType implements EnumWithSmallCode {
	ACTIVE_DIRECTORY("AD"), E_DIRECTORY("ED");

	private final String code;


	DirectoryType(String code) {
		this.code = code;
	}

	public String getCode(){
		return code;
	}
}
