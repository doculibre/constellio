package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.entities.MetadataVO;

import java.util.Locale;

public class MetadataFieldFactoryBuildExtensionParams {

	private MetadataVO metadata;
	private String recordId;
	private Locale locale;

	public MetadataFieldFactoryBuildExtensionParams(MetadataVO metadata, String recordId, Locale locale) {
		this.metadata = metadata;
		this.recordId = recordId;
		this.locale = locale;
	}

	public MetadataVO getMetadataVO() {
		return metadata;
	}

	public String getRecordId() {
		return recordId;
	}

	public Locale getLocale() {
		return locale;
	}

}
