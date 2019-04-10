package com.constellio.app.extensions.records.params;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.records.Record;

public interface IsMetadataSpecialCaseToNotBeShownParams {
	MetadataVO getMetadataVO();

	Record getRecord();
}
