package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.wrappers.User;

public class GetSearchResultSimpleTableWindowComponentParam {

	private RecordVO recordVO;
	private User user;

	public GetSearchResultSimpleTableWindowComponentParam(RecordVO recordVO, User user) {
		this.recordVO = recordVO;
		this.user = user;
	}

	public String getSchemaType() {
		return recordVO.getSchema().getTypeCode();
	}

	public RecordVO getRecordVO() {
		return recordVO;
	}

	public User getUser() {
		return user;
	}
}
