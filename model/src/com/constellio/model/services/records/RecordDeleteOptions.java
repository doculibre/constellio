package com.constellio.model.services.records;

public class RecordDeleteOptions {

	boolean referencesToNull;

	boolean referenecesToNullWhenPossible;

	public boolean isReferencesToNull() {
		return referencesToNull;
	}

	public RecordDeleteOptions setReferencesToNull(boolean referencesToNull) {
		this.referencesToNull = referencesToNull;
		return this;
	}

	public boolean isReferenecesToNullWhenPossible() {
		return referenecesToNullWhenPossible;
	}

	public RecordDeleteOptions setReferenecesToNullWhenPossible(boolean referenecesToNullWhenPossible) {
		this.referenecesToNullWhenPossible = referenecesToNullWhenPossible;
		return this;
	}
}
