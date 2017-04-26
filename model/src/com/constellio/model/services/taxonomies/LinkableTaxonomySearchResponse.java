package com.constellio.model.services.taxonomies;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.SPEQueryResponse;

public class LinkableTaxonomySearchResponse {

	private long numFound;

	private List<TaxonomySearchRecord> records;

	private long qTime;

	private final FastContinueInfos fastContinueInfos;

	public LinkableTaxonomySearchResponse(long numFound,
			List<TaxonomySearchRecord> records) {
		this.numFound = numFound;
		this.fastContinueInfos = null;
		this.records = records;
	}

	public LinkableTaxonomySearchResponse(long numFound, FastContinueInfos fastContinueInfos,
			List<TaxonomySearchRecord> records) {
		this.numFound = numFound;
		this.fastContinueInfos = fastContinueInfos;
		this.records = records;
	}

	public long getNumFound() {
		return numFound;
	}

	public long getQTime() {
		return qTime;
	}

	public FastContinueInfos getFastContinueInfos() {
		return fastContinueInfos;
	}

	public List<TaxonomySearchRecord> getRecords() {
		return records;
	}

	public LinkableTaxonomySearchResponse withQTime(long qTime) {
		this.qTime = qTime;
		return this;
	}

	public static LinkableTaxonomySearchResponse asUnlinkableWithChildrenRecords(SPEQueryResponse response) {

		List<TaxonomySearchRecord> returnedRecords = new ArrayList<>();
		for (Record record : response.getRecords()) {
			returnedRecords.add(new TaxonomySearchRecord(record, false, true));
		}

		return new LinkableTaxonomySearchResponse(response.getNumFound(), returnedRecords);
	}

}
