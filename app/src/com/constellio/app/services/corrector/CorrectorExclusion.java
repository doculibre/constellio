package com.constellio.app.services.corrector;

public class CorrectorExclusion {
	private String collection;
	private String exclusion;

	public String getCollection() {
		return collection;
	}

	public CorrectorExclusion setCollection(String collection) {
		this.collection = collection;
		return this;
	}

	public String getExclusion() {
		return exclusion;
	}

	public CorrectorExclusion setExclusion(String exclusion) {
		this.exclusion = exclusion;
		return this;
	}
}
