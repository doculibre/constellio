package com.constellio.app.ui.framework.builders;

import com.constellio.app.ui.entities.BagInfoVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;

import java.util.List;

public class BagInfoToVOBuilder extends RecordToVOBuilder {
	@Override
	protected BagInfoVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, RecordVO.VIEW_MODE viewMode) {
		return new BagInfoVO(id, metadataValueVOs, viewMode);
	}

	@Override
	public BagInfoVO build(Record record, RecordVO.VIEW_MODE viewMode, SessionContext sessionContext) {
		return (BagInfoVO) super.build(record, viewMode, sessionContext);
	}
}
