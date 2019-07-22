package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.model.services.records.cache.hooks.DeterminedHookCacheInsertion;

public class CacheInsertionResponse {

	CacheInsertionStatus status;

	RecordDTO summaryRecordDTO;

	DeterminedHookCacheInsertion determinedHookCacheInsertion;

	public CacheInsertionResponse(CacheInsertionStatus status, RecordDTO summaryRecordDTO, DeterminedHookCacheInsertion determinedHookCacheInsertion) {
		this.status = status;
		this.summaryRecordDTO = summaryRecordDTO;
		this.determinedHookCacheInsertion = determinedHookCacheInsertion;
	}

	public CacheInsertionStatus getStatus() {
		return status;
	}

	public RecordDTO getSummaryRecordDTO() {
		return summaryRecordDTO;
	}

	public DeterminedHookCacheInsertion getDeterminedHookCacheInsertion() {
		return determinedHookCacheInsertion;
	}
}
