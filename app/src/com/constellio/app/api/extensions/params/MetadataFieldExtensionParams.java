package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;

public class MetadataFieldExtensionParams {
	MetadataVO metadataVO = null;
	RecordVO recordVO = null;
	Object extraParam;

	public MetadataFieldExtensionParams(MetadataVO metadataVO) {
		this(metadataVO, null);
	}

	public MetadataFieldExtensionParams(RecordVO recordVO) {
		this(null, recordVO);
	}

	public MetadataFieldExtensionParams(MetadataVO metadataVO, RecordVO recordVO) {
		this(metadataVO, recordVO, null);
	}

	public MetadataFieldExtensionParams(MetadataVO metadataVO, RecordVO recordVO, Object extraParam) {
		this.metadataVO = metadataVO;
		this.recordVO = recordVO;
		this.extraParam = extraParam;
	}

	public Object getExtraParam() {
		return extraParam;
	}

	public MetadataVO getMetadataVO() {
		return metadataVO;
	}

	public RecordVO getRecordVO() {
		return recordVO;
	}
}
