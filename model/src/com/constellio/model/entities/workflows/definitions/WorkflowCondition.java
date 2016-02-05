package com.constellio.model.entities.workflows.definitions;

import com.constellio.model.entities.workflows.execution.WorkflowExecution;

public interface WorkflowCondition {

	boolean isTrue(WorkflowExecution execution);

}
