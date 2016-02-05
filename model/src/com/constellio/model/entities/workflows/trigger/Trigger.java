package com.constellio.model.entities.workflows.trigger;

public class Trigger {

	TriggerType triggerType;

	String triggeredSchemaCode;

	String triggeredMetadataCode;

	ActionCompletion actionCompletion;

	public Trigger(TriggerType triggerType, String triggeredSchemaCode, String triggeredMetadataCode,
			ActionCompletion actionCompletion) {
		this.triggerType = triggerType;
		this.triggeredSchemaCode = triggeredSchemaCode;
		this.triggeredMetadataCode = triggeredMetadataCode;
		this.actionCompletion = actionCompletion;
	}

	public static Trigger manual(String schemaCode) {
		return new Trigger(TriggerType.MANUAL, schemaCode, null, ActionCompletion.SUSPEND);
	}

	public TriggerType getTriggerType() {
		return triggerType;
	}

	public String getTriggeredSchemaCode() {
		return triggeredSchemaCode;
	}

	public String getTriggeredMetadataCode() {
		return triggeredMetadataCode;
	}

	public ActionCompletion getActionCompletion() {
		return actionCompletion;
	}
}
