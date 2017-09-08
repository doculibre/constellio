package com.constellio.app.modules.tasks.services;

import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowTaskVO;

public class BetaWorkflowServicesRuntimeException extends RuntimeException {

	public BetaWorkflowServicesRuntimeException(String message) {
		super(message);
	}

	public BetaWorkflowServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public BetaWorkflowServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class WorkflowServicesRuntimeException_UnsupportedMove extends BetaWorkflowServicesRuntimeException {

		public WorkflowServicesRuntimeException_UnsupportedMove(String message) {
			super(message);
		}
	}

	public static class WorkflowServicesRuntimeException_UnsupportedAddAtPosition extends BetaWorkflowServicesRuntimeException {

		public WorkflowServicesRuntimeException_UnsupportedAddAtPosition(String id, String decision, Throwable t) {
			super("Cannot add task at position '" + id + "#" + decision + "'", t);
		}

		public WorkflowServicesRuntimeException_UnsupportedAddAtPosition(BetaWorkflowTaskVO taskVO, Throwable t) {
			super("Cannot add task at position '" + taskVO.getId() + "#" + taskVO.getDecision() + "'", t);
		}
	}
}
