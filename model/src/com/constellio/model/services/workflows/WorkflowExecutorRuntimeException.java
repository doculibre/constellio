package com.constellio.model.services.workflows;

public class WorkflowExecutorRuntimeException extends RuntimeException {

	public WorkflowExecutorRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public WorkflowExecutorRuntimeException(String message) {
		super(message);
	}

	public static class WorkflowExecutorRuntimeException_InvalidTransaction extends WorkflowExecutorRuntimeException {

		public WorkflowExecutorRuntimeException_InvalidTransaction(Throwable cause) {
			super("Error while executing transaction", cause);
		}
	}

	public static class WorkflowExecutorRuntimeException_InvalidTaskId extends WorkflowExecutorRuntimeException {

		public WorkflowExecutorRuntimeException_InvalidTaskId(String taskId) {
			super("Invalid task identifier: '" + taskId + "'.");
		}
	}
}
