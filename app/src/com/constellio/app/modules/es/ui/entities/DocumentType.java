package com.constellio.app.modules.es.ui.entities;

import java.io.Serializable;

public class DocumentType implements Serializable {
	private final String code;
	private final String label;

	public DocumentType(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}
}
