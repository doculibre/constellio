package com.constellio.app.modules.es.services.mapping;

import java.io.Serializable;

public class MappingParams implements Serializable {
	private final String fieldId;
	private final TargetParams target;
	private boolean active;

	public MappingParams(String fieldId, TargetParams target) {
		this.fieldId = fieldId;
		this.target = target;
		active = true;
	}

	public String getFieldId() {
		return fieldId;
	}

	public TargetParams getTarget() {
		return target;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
