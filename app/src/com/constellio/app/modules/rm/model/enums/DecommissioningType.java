package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum DecommissioningType implements EnumWithSmallCode {

	TRANSFERT_TO_SEMI_ACTIVE("T"), DEPOSIT("C"), DESTRUCTION("D");

	private String code;

	DecommissioningType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
