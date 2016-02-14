package com.constellio.app.modules.tasks.services;

import com.constellio.app.modules.tasks.ui.entities.WorkflowTaskVO;

public class WorkflowServicesRuntimeException extends RuntimeException {

	public WorkflowServicesRuntimeException(String message) {
		super(message);
	}

	public WorkflowServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public WorkflowServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class WorkflowServicesRuntimeException_UnsupportedMove extends WorkflowServicesRuntimeException {

		public WorkflowServicesRuntimeException_UnsupportedMove(String message) {
			super(message);
		}
	}

	public static class WorkflowServicesRuntimeException_UnsupportedAddAtPosition extends WorkflowServicesRuntimeException {

		public WorkflowServicesRuntimeException_UnsupportedAddAtPosition(String id, String decision, Throwable t) {
			super("Cannot add task at position '" + id + "#" + decision + "'", t);
		}

		public WorkflowServicesRuntimeException_UnsupportedAddAtPosition(WorkflowTaskVO taskVO, Throwable t) {
			super("Cannot add task at position '" + taskVO.getId() + "#" + taskVO.getDecision() + "'", t);
		}
	}
}
