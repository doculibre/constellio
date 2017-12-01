package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.entities.RecordVO;

public class GetSearchResultSimpleTableWindowComponentParam {

	private RecordVO recordVO;

	public GetSearchResultSimpleTableWindowComponentParam(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	public String getSchemaType() {
		return recordVO.getSchema().getTypeCode();
	}

	public RecordVO getRecordVO() {
		return recordVO;
	}

}
