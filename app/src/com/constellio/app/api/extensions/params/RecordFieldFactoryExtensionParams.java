package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.framework.components.MetadataFieldFactory;

public class RecordFieldFactoryExtensionParams {
	
	private String key;
	
	private MetadataFieldFactory metadataFieldFactory;

	public RecordFieldFactoryExtensionParams(String key, MetadataFieldFactory metadataFieldFactory) {
		this.key = key;
		this.metadataFieldFactory = metadataFieldFactory;
	}
	
	public String getKey() {
		return key;
	}

	public MetadataFieldFactory getMetadataFieldFactory() {
		return metadataFieldFactory;
	}

}
