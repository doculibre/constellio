package com.constellio.app.modules.tasks.ui.entities;

import static com.constellio.app.modules.tasks.model.wrappers.Workflow.CODE;

import java.util.List;

import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;

public class WorkflowVO extends RecordVO {

	public WorkflowVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
		super(id, metadataValues, viewMode);
	}

	public WorkflowVO(RecordVO recordVO) {
		super(recordVO.getId(), recordVO.getMetadataValues(), recordVO.getViewMode());
	}

	public String getCode() {
		return get(CODE);
	}

	public void setCode(String code) {
		set(CODE, code);
	}

}
