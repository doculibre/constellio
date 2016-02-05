package com.constellio.model.services.schemas.testimpl;

import com.constellio.model.entities.schemas.ModifiableStructure;

public class ZeModifiableStructure implements ModifiableStructure {

	private String value;

	public ZeModifiableStructure(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public boolean isDirty() {
		return false;
	}
}
