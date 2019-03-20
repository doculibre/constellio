package com.constellio.app.extensions.records.params;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.records.Record;

public interface isMetadataSpecialCaseOfNotBeingInFormParams {
	MetadataVO getMetadataVO();

	Record getRecord();
}
