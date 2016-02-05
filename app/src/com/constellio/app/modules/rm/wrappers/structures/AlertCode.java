package com.constellio.app.modules.rm.wrappers.structures;

import com.constellio.model.entities.EnumWithSmallCode;

public enum AlertCode implements EnumWithSmallCode {

	LATE_FOLDER("F"), DECOMMISSIONING("D");

	private String code;

	AlertCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
