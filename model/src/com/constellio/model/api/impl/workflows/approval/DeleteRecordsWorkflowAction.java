package com.constellio.model.api.impl.workflows.approval;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.workflows.definitions.WorkflowAction;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.model.services.factories.ModelLayerFactory;

public class DeleteRecordsWorkflowAction implements WorkflowAction {
	@Override
	public void execute(WorkflowExecution workflowExecution, ModelLayerFactory modelLayerFactory) {
		for (String recordId : workflowExecution.getRecordIds()) {
			Record recordToDelete = modelLayerFactory.newRecordServices().getDocumentById(recordId);
			modelLayerFactory.newRecordServices().logicallyDelete(recordToDelete, User.GOD);
		}
	}
}
