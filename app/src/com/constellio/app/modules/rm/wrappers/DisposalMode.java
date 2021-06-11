package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.EnumWithSmallCode;

public enum DisposalMode implements EnumWithSmallCode {
	COPY("COPY"), ARCHIVE("ARCHIVE");

	private final String code;

	DisposalMode(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}

}