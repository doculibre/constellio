package com.constellio.model.services.workflows.general;

import com.constellio.model.entities.workflows.definitions.WorkflowCondition;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;

public class WorkflowConditions {

	public static WorkflowCondition directCondition() {
		return new WorkflowCondition() {
			@Override
			public boolean isTrue(WorkflowExecution execution) {
				return true;
			}
		};
	}

	public static WorkflowCondition approvalCondition() {
		return new WorkflowCondition() {
			@Override
			public boolean isTrue(WorkflowExecution execution) {
				return execution.getVariable("decision").equals("approved");
			}
		};
	}

}
