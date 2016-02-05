package com.constellio.app.modules.es.model.connectors.http.enums;

import com.constellio.model.entities.EnumWithSmallCode;

public enum FetchFrequency implements EnumWithSmallCode {

	ALWAYS("a"), HOURLY("h"), DAILY("d"), WEEKLY("w"), MONTHLY("m"), YEARLY("y");

	private String code;

	FetchFrequency(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
