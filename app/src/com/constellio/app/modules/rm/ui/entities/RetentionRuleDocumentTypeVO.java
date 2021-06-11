package com.constellio.app.modules.rm.ui.entities;

import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;

import java.util.List;

public class RetentionRuleDocumentTypeVO extends RecordVO {
	public RetentionRuleDocumentTypeVO(String id,
									   List<MetadataValueVO> metadataValues,
									   VIEW_MODE viewMode) {
		super(id, metadataValues, viewMode);
	}

	public RetentionRuleDocumentTypeVO(String id, List<MetadataValueVO> metadataValues,
									   VIEW_MODE viewMode, List<String> excludedMetadataCodeList) {
		super(id, metadataValues, viewMode, excludedMetadataCodeList);
	}
}
