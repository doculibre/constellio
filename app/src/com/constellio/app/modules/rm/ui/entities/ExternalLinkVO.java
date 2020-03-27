package com.constellio.app.modules.rm.ui.entities;

import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;

import java.util.List;

public class ExternalLinkVO extends RecordVO {

	public ExternalLinkVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode,
						  List<String> excludedMetadata) {
		super(id, metadataValues, viewMode, excludedMetadata);
	}

	public ExternalLinkVO(RecordVO recordVO) {
		super(recordVO.getId(), recordVO.getMetadataValues(), recordVO.getViewMode());
	}

}
