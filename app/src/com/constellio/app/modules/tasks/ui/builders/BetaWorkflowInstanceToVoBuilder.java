package com.constellio.app.modules.tasks.ui.builders;

import java.util.List;

import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowInstanceVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;

public class BetaWorkflowInstanceToVoBuilder extends RecordToVOBuilder {
	@Override
	public BetaWorkflowInstanceVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		return (BetaWorkflowInstanceVO) super.build(record, viewMode, sessionContext);
	}

	@Override
	public BetaWorkflowInstanceVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO, SessionContext sessionContext) {
		return (BetaWorkflowInstanceVO) super.build(record, viewMode, schemaVO, sessionContext);
	}

	@Override
	protected RecordVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode) {
		return new BetaWorkflowInstanceVO(id, metadataValueVOs, viewMode);
	}
}
