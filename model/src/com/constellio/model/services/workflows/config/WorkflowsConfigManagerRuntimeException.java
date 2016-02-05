package com.constellio.model.services.workflows.config;

@SuppressWarnings("serial")
public class WorkflowsConfigManagerRuntimeException extends RuntimeException {

	public WorkflowsConfigManagerRuntimeException() {
		super();
	}

	public WorkflowsConfigManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public WorkflowsConfigManagerRuntimeException(String message) {
		super(message);
	}

	public WorkflowsConfigManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class WorkflowsConfigManagerRuntimeException_InvalidWorkflowConfiguration extends
																							WorkflowsConfigManagerRuntimeException {
		public WorkflowsConfigManagerRuntimeException_InvalidWorkflowConfiguration() {
			super("MetadataCode cannot be null");
		}
	}

	public static class WorkflowsConfigManagerRuntimeException_WorkflowConfigurationAlreadyExists extends
																								  WorkflowsConfigManagerRuntimeException {
		public WorkflowsConfigManagerRuntimeException_WorkflowConfigurationAlreadyExists(String schemaCode, String triggerType) {
			super("Workflow configuration already exists for schemaCode '" + schemaCode + "' and triggerType '" + triggerType
					+ "'");
		}
	}

	public static class WorkflowsConfigManagerRuntimeException_NoWorkflowConfigurationForThisCollection extends
																										WorkflowsConfigManagerRuntimeException {
		public WorkflowsConfigManagerRuntimeException_NoWorkflowConfigurationForThisCollection(String collection) {
			super("No workflow configuration for this site '" + collection + "'");
		}
	}

}
