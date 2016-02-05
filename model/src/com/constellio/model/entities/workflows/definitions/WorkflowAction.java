package com.constellio.model.entities.workflows.definitions;

import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.model.services.factories.ModelLayerFactory;

public interface WorkflowAction {

	void execute(WorkflowExecution workflowExecution, ModelLayerFactory modelLayerFactory);

}
