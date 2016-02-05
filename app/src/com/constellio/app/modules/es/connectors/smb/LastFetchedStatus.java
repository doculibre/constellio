package com.constellio.app.modules.es.connectors.smb;

import com.constellio.model.entities.EnumWithSmallCode;

public enum LastFetchedStatus implements EnumWithSmallCode {
	OK("OK"), FAILED("FAIL"), PARTIAL("PAR");

	private final String code;

	LastFetchedStatus(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}

}