package com.constellio.app.extensions.records.params;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;

public class GetIconPathParams {
	
	private final Record record;
	private final RecordVO recordVO;
	private final boolean expanded;

	public GetIconPathParams(Record record, boolean expanded) {
		this.record = record;
		this.recordVO = null;
		this.expanded = expanded;
	}

	public GetIconPathParams(RecordVO recordVO, boolean expanded) {
		this.record = null;
		this.recordVO = recordVO;
		this.expanded = expanded;
	}

	public Record getRecord() {
		return record;
	}

	public RecordVO getRecordVO() {
		return recordVO;
	}

	public boolean isExpanded() {
		return expanded;
	}
}
