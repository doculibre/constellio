package com.constellio.model.services.records;

public class RecordDeleteOptions {

	boolean setMostReferencesToNull;

	public boolean isSetMostReferencesToNull() {
		return setMostReferencesToNull;
	}

	public RecordDeleteOptions setMostReferencesToNull(boolean setMostReferencesToNull) {
		this.setMostReferencesToNull = setMostReferencesToNull;
		return this;
	}

}
