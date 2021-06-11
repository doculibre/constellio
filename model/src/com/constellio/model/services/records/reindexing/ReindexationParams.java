package com.constellio.model.services.records.reindexing;

import com.constellio.data.dao.dto.records.RecordId;

import java.util.List;

public class ReindexationParams {

	private boolean background;
	private boolean includeSolrPrivateKey;
	private ReindexationMode reindexationMode;
	private int batchSize = 0;
	private boolean repopulate = true;
	private boolean multithreading = true;
	private List<RecordId> limitToHierarchyOf;

	public ReindexationParams(ReindexationMode reindexationMode) {
		this.reindexationMode = reindexationMode;
	}

	public ReindexationParams setReindexationMode(
			ReindexationMode reindexationMode) {
		this.reindexationMode = reindexationMode;
		return this;
	}

	public ReindexationMode getReindexationMode() {
		return reindexationMode;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public ReindexationParams setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}


	public boolean isRepopulate() {
		return repopulate;
	}

	public ReindexationParams setRepopulate(boolean repopulate) {
		this.repopulate = repopulate;
		return this;
	}

	public boolean isMultithreading() {
		return multithreading;
	}

	public ReindexationParams setMultithreading(boolean multithreading) {
		this.multithreading = multithreading;
		return this;
	}

	public List<RecordId> getLimitToHierarchyOf() {
		return limitToHierarchyOf;
	}

	public ReindexationParams setLimitToHierarchyOf(
			List<RecordId> limitToHierarchyOf) {
		this.limitToHierarchyOf = limitToHierarchyOf;
		return this;
	}

	public boolean isIncludeSolrPrivateKey() {
		return includeSolrPrivateKey;
	}

	public ReindexationMode setIncludeSolrPrivateKey(boolean includeSolrPrivateKey) {
		this.includeSolrPrivateKey = includeSolrPrivateKey;
		return reindexationMode;
	}
}
