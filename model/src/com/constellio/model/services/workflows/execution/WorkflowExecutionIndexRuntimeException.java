package com.constellio.model.services.workflows.execution;

@SuppressWarnings("serial")
public class WorkflowExecutionIndexRuntimeException extends RuntimeException {

	public WorkflowExecutionIndexRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public WorkflowExecutionIndexRuntimeException(String message) {
		super(message);
	}

	public static class WorkflowExecutionIndexRuntimeException_WorkflowExecutionNotFound
			extends WorkflowExecutionIndexRuntimeException {

		public WorkflowExecutionIndexRuntimeException_WorkflowExecutionNotFound(String id, String collection) {
			super("WorkflowExecution '" + id + "' not found in collection:" + collection);
		}
	}
}
