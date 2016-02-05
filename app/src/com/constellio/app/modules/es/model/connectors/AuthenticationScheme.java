package com.constellio.app.modules.es.model.connectors;

import com.constellio.model.entities.EnumWithSmallCode;

public enum AuthenticationScheme implements EnumWithSmallCode {
	BASIC("BASIC"), NTLM("NTLM");

	private final String code;

	AuthenticationScheme(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}
}
