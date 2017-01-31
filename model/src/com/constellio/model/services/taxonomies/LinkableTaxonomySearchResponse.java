package com.constellio.model.services.taxonomies;

import java.util.List;

public class LinkableTaxonomySearchResponse {

	private long numFound;

	private List<TaxonomySearchRecord> records;

	private long qTime;

	private final int lastVisibleIndex;

	public LinkableTaxonomySearchResponse(long numFound,
			List<TaxonomySearchRecord> records) {
		this.numFound = numFound;
		this.lastVisibleIndex = -1;
		this.records = records;
	}

	public LinkableTaxonomySearchResponse(long numFound, int lastVisibleIndex,
			List<TaxonomySearchRecord> records) {
		this.numFound = numFound;
		this.lastVisibleIndex = lastVisibleIndex;
		this.records = records;
	}

	public long getNumFound() {
		return numFound;
	}

	public long getQTime() {
		return qTime;
	}

	public int getLastVisibleIndex() {
		return lastVisibleIndex;
	}

	public List<TaxonomySearchRecord> getRecords() {
		return records;
	}

	public LinkableTaxonomySearchResponse withQTime(long qTime) {
		this.qTime = qTime;
		return this;
	}
}
