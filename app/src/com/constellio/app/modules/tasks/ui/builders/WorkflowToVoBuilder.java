package com.constellio.app.modules.tasks.ui.builders;

import java.util.List;

import com.constellio.app.modules.tasks.ui.entities.WorkflowVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;

public class WorkflowToVoBuilder extends RecordToVOBuilder {
	@Override
	public WorkflowVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		return (WorkflowVO) super.build(record, viewMode, sessionContext);
	}

	@Override
	public WorkflowVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO, SessionContext sessionContext) {
		return (WorkflowVO) super.build(record, viewMode, schemaVO, sessionContext);
	}

	@Override
	protected RecordVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode) {
		return new WorkflowVO(id, metadataValueVOs, viewMode);
	}

}
