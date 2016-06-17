package com.constellio.model.services.records;

public class RecordDeleteOptions {

	boolean setAllReferencesToNull;

	boolean setMostReferencesToNull;

	public boolean isSetAllReferencesToNull() {
		return setAllReferencesToNull;
	}

	public RecordDeleteOptions setAllReferencesToNull(boolean referencesToNull) {
		this.setAllReferencesToNull = referencesToNull;
		return this;
	}

	public boolean isSetMostReferencesToNull() {
		return setMostReferencesToNull;
	}

	public RecordDeleteOptions setMostReferencesToNull(boolean setMostReferencesToNull) {
		this.setMostReferencesToNull = setMostReferencesToNull;
		return this;
	}
}
