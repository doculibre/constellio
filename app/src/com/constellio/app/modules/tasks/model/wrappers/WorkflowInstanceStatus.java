package com.constellio.app.modules.tasks.model.wrappers;

import com.constellio.model.entities.EnumWithSmallCode;

/**
 * *** USED BY BETA_WORKFLOW BETA ***
 */
public enum WorkflowInstanceStatus implements EnumWithSmallCode {

	IN_PROGRESS("I"), CANCELLED("C"), FINISHED("F");

	private String code;

	WorkflowInstanceStatus(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
