package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordDTO;

public class CacheInsertionResponse {

	CacheInsertionStatus status;

	RecordDTO summaryRecordDTO;

	public CacheInsertionResponse(CacheInsertionStatus status, RecordDTO summaryRecordDTO) {
		this.status = status;
		this.summaryRecordDTO = summaryRecordDTO;
	}

	public CacheInsertionStatus getStatus() {
		return status;
	}

	public RecordDTO getSummaryRecordDTO() {
		return summaryRecordDTO;
	}
}
