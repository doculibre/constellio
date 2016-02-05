package com.constellio.app.modules.rm.model.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum FolderMediaType implements EnumWithSmallCode {

	ANALOG("A"), HYBRID("H"), UNKNOWN("U"), ELECTRONIC("E");

	private String code;

	FolderMediaType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public boolean potentiallyHasAnalogMedium() {
		return this == ANALOG || this == HYBRID || this == UNKNOWN;
	}

	public boolean potentiallyHasElectronicMedium() {
		return this == ELECTRONIC || this == HYBRID || this == UNKNOWN;
	}
}
