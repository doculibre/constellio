package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;

public class MetadataFieldExtensionParams {
	MetadataVO metadataVO = null;
	RecordVO recordVO = null;

	public MetadataFieldExtensionParams(MetadataVO metadataVO) {
		this.metadataVO = metadataVO;
	}

	public MetadataFieldExtensionParams(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	public MetadataFieldExtensionParams(MetadataVO metadataVO, RecordVO recordVO) {
		this.metadataVO = metadataVO;
		this.recordVO = recordVO;
	}

	public MetadataVO getMetadataVO() {
		return metadataVO;
	}

	public RecordVO getRecordVO() {
		return recordVO;
	}
}
