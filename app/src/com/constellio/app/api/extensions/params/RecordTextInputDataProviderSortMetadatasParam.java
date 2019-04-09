package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.pages.base.SessionContextProvider;

public class RecordTextInputDataProviderSortMetadatasParam {

	private String schemaCode;
	private String schemaTypeCode;
	private SessionContextProvider sessionContextProvider;

	public RecordTextInputDataProviderSortMetadatasParam(String schemaCode, String schemaTypeCode, SessionContextProvider sessionContextProvider) {
		this.schemaCode = schemaCode;
		this.schemaTypeCode = schemaTypeCode;
		this.sessionContextProvider = sessionContextProvider;
	}

	public String getSchemaCode() {
		return schemaCode;
	}

	public String getSchemaTypeCode() {
		return schemaTypeCode;
	}

	public SessionContextProvider getSessionContextProvider() {
		return sessionContextProvider;
	}

}
