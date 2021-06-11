package com.constellio.app.modules.rm.services.reports.printable;

import com.constellio.model.entities.EnumWithSmallCode;

public enum PrintableExtension implements EnumWithSmallCode {
	PDF("p"),
	DOCX("w"),
	XLSX("e"),
	HTML("h");

	private final String code;

	PrintableExtension(String code) {
		this.code = code;
	}

	public String getExtension() {
		return "." + name().toLowerCase();
	}

	@Override
	public String getCode() {
		return code;
	}
}
