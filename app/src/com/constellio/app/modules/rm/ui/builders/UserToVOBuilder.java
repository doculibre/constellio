package com.constellio.app.modules.rm.ui.builders;

import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;

import java.util.List;

public class UserToVOBuilder extends RecordToVOBuilder {

	@Override
	public UserVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		return (UserVO) super.build(record, viewMode, sessionContext);
	}

	@Override
	protected UserVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode) {
		return new UserVO(id, metadataValueVOs, viewMode);
	}

}
