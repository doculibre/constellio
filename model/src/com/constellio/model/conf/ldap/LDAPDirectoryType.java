package com.constellio.model.conf.ldap;

public enum LDAPDirectoryType {

	ACTIVE_DIRECTORY("AD"), E_DIRECTORY("eDirectory"), AZURE_AD("azur");

	private final String code;


	LDAPDirectoryType(String code) {
		this.code = code;
	}

	public String getCode(){
		return code;
	}

}
