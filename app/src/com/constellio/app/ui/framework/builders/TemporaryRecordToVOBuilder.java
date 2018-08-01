package com.constellio.app.ui.framework.builders;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.TemporaryRecordVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;

import java.util.List;

public class TemporaryRecordToVOBuilder extends RecordToVOBuilder {

	@Override
	public TemporaryRecordVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		return (TemporaryRecordVO) super.build(record, viewMode, sessionContext);
	}

	@Override
	public TemporaryRecordVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO,
								   SessionContext sessionContext) {
		return (TemporaryRecordVO) super.build(record, viewMode, schemaVO, sessionContext);
	}

	@Override
	protected TemporaryRecordVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode) {
		return new TemporaryRecordVO(id, metadataValueVOs, viewMode);
	}

}
