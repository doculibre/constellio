package com.constellio.app.modules.tasks.ui.entities;

import static com.constellio.app.modules.tasks.model.wrappers.WorkflowInstance.STARTED_BY;
import static com.constellio.app.modules.tasks.model.wrappers.WorkflowInstance.STARTED_ON;
import static com.constellio.app.modules.tasks.model.wrappers.WorkflowInstance.STATUS;
import static com.constellio.app.modules.tasks.model.wrappers.WorkflowInstance.WORKFLOW;

import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.app.modules.tasks.model.wrappers.Workflow;
import com.constellio.app.modules.tasks.model.wrappers.WorkflowInstanceStatus;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class WorkflowInstanceVO extends RecordVO {

	public WorkflowInstanceVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
		super(id, metadataValues, viewMode);
	}

	public WorkflowInstanceVO(RecordVO recordVO) {
		super(recordVO.getId(), recordVO.getMetadataValues(), recordVO.getViewMode());
	}

	public String getWorkflow() {
		return get(WORKFLOW);
	}

	public void setWorkflow(String workflowId) {
		set(WORKFLOW, workflowId);
	}

	public void setWorkflow(Record workflow) {
		set(WORKFLOW, workflow);
	}

	public void setWorkflow(Workflow workflow) {
		set(WORKFLOW, workflow);
	}

	public WorkflowInstanceStatus getWorkflowStatus() {
		return get(STATUS);
	}

	public void setWorkflowStatus(WorkflowInstanceStatus status) {
		set(STATUS, status);
	}

	public LocalDateTime getStartedOn() {
		return get(STARTED_ON);
	}

	public void setStartedOn(LocalDateTime startedOn) {
		set(STARTED_ON, startedOn);
	}

	public String getStartedBy() {
		return get(STARTED_BY);
	}

	public void setStartedBy(String startedBy) {
		set(STARTED_BY, startedBy);
	}

	public void setStartedBy(Record startedBy) {
		set(STARTED_BY, startedBy);
	}

	public void setStartedBy(User startedBy) {
		set(STARTED_BY, startedBy);
	}

}
