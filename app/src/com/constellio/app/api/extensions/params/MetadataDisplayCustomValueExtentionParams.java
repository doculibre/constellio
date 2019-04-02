package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;

public class MetadataDisplayCustomValueExtentionParams {
	private Object displayValue;
	private RecordVO recordVO;
	private MetadataVO metadataVO;

	public MetadataDisplayCustomValueExtentionParams(Object displayValue, RecordVO recordVO,
			MetadataVO metadataVO) {
		this.displayValue = displayValue;
		this.recordVO = recordVO;
		this.metadataVO = metadataVO;
	}

	public Object getDisplayValue() {
		return displayValue;
	}

	public RecordVO getRecordVO() {
		return recordVO;
	}

	public MetadataVO getMetadataVO() {
		return metadataVO;
	}
}
