package com.constellio.app.services.extensions.plugins.pluginInfo;

import com.constellio.model.entities.EnumWithSmallCode;

public enum ConstellioPluginStatus implements EnumWithSmallCode {
	READY_TO_INSTALL("R"), INVALID("I"), ENABLED("E"), DISABLED("D");

	private String code;

	ConstellioPluginStatus(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
