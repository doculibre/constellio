package com.constellio.app.modules.tasks.ui.builders;

import java.util.List;

import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;

public class BetaWorkflowToVoBuilder extends RecordToVOBuilder {
	@Override
	public BetaWorkflowVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		return (BetaWorkflowVO) super.build(record, viewMode, sessionContext);
	}

	@Override
	public BetaWorkflowVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO, SessionContext sessionContext) {
		return (BetaWorkflowVO) super.build(record, viewMode, schemaVO, sessionContext);
	}

	@Override
	protected RecordVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode) {
		return new BetaWorkflowVO(id, metadataValueVOs, viewMode);
	}

}
