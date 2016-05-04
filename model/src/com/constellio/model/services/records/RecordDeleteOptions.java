package com.constellio.model.services.records;

public class RecordDeleteOptions {

	boolean referencesToNull;

	public boolean isReferencesToNull() {
		return referencesToNull;
	}

	public RecordDeleteOptions setReferencesToNull(boolean referencesToNull) {
		this.referencesToNull = referencesToNull;
		return this;
	}
}
