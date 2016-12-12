package com.constellio.app.api.extensions.params;

import com.constellio.app.services.records.SystemCheckResults;
import com.constellio.app.services.records.SystemCheckResultsBuilder;

public class CollectionSystemCheckParams {

	String collection;
	SystemCheckResultsBuilder resultsBuilder;
	boolean repair;

	public CollectionSystemCheckParams(String collection, SystemCheckResultsBuilder resultsBuilder, boolean repair) {
		this.collection = collection;
		this.resultsBuilder = resultsBuilder;
		this.repair = repair;
	}

	public String getCollection() {
		return collection;
	}

	public SystemCheckResultsBuilder getResultsBuilder() {
		return resultsBuilder;
	}

	public boolean isRepair() {
		return repair;
	}

}
