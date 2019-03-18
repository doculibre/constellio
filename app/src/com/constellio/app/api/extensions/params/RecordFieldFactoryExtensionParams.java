package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;

public class RecordFieldFactoryExtensionParams {

	private String key;
	private MetadataFieldFactory metadataFieldFactory;
	private RecordVO recordVO;

	public RecordFieldFactoryExtensionParams(String key, MetadataFieldFactory metadataFieldFactory, RecordVO recordVO) {
		this.key = key;
		this.metadataFieldFactory = metadataFieldFactory;
		this.recordVO = recordVO;
	}

	public String getKey() {
		return key;
	}

	public MetadataFieldFactory getMetadataFieldFactory() {
		return metadataFieldFactory;
	}

	public RecordVO getRecordVO() {
		return recordVO;
	}

}
