package com.constellio.model.services.taxonomies;

import java.util.List;

public class LinkableTaxonomySearchResponse {

	private long numFound;

	private List<TaxonomySearchRecord> records;

	private long qTime;

	public LinkableTaxonomySearchResponse(long numFound,
			List<TaxonomySearchRecord> records) {
		this.numFound = numFound;
		this.records = records;
	}

	public long getNumFound() {
		return numFound;
	}

	public long getQTime() {
		return qTime;
	}

	public List<TaxonomySearchRecord> getRecords() {
		return records;
	}

	public LinkableTaxonomySearchResponse withQTime(long qTime) {
		this.qTime = qTime;
		return this;
	}
}
