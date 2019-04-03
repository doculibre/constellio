package com.constellio.app.entities.system;

import com.constellio.model.entities.EnumWithSmallCode;

public enum SystemState implements EnumWithSmallCode {

	OK("O"), WARNING("W"), CRITIC("C");

	private String code;

	SystemState(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		return code;
	}

	public static SystemState getHighestUrgency(SystemState state1, SystemState state2) {
		if(state1 == CRITIC || state2 == CRITIC) {
			return CRITIC;
		} else if(state1 == WARNING || state2 == WARNING) {
			return WARNING;
		} else {
			return OK;
		}
	}
}
