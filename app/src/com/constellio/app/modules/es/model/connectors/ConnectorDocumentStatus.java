package com.constellio.app.modules.es.model.connectors;

import com.constellio.model.entities.EnumWithSmallCode;

public enum ConnectorDocumentStatus implements EnumWithSmallCode {

	UNFETCHED("uf"), OK("ok"), ERROR("er");

	private String code;

	ConnectorDocumentStatus(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
