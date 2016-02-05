package com.constellio.app.modules.tasks.model.wrappers;

import com.constellio.model.entities.EnumWithSmallCode;

public enum TaskStatusType implements EnumWithSmallCode {

	STANDBY("S"), IN_PROGRESS("I"), FINISHED("F"), CLOSED("C");

	private String code;

	TaskStatusType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public boolean isFinished() {
		return this == FINISHED;
	}

	public boolean isAfterFinished() {
		return this == CLOSED;
	}

	public boolean isBeforeFinished() {
		return this == STANDBY || this == IN_PROGRESS;
	}

	public boolean isAfterStandby() {
		return this != STANDBY;
	}
}
