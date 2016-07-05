package com.constellio.model.services.records;

public class RecordPhysicalDeleteOptions {

	boolean setMostReferencesToNull;

	public boolean isSetMostReferencesToNull() {
		return setMostReferencesToNull;
	}

	public RecordPhysicalDeleteOptions setMostReferencesToNull(boolean setMostReferencesToNull) {
		this.setMostReferencesToNull = setMostReferencesToNull;
		return this;
	}

}
