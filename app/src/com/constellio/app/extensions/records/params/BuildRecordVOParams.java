package com.constellio.app.extensions.records.params;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;

public class BuildRecordVOParams {
	private final Record record;
	private final RecordVO builtRecordVO;

	public BuildRecordVOParams(Record record, RecordVO builtRecordVO) {
		this.record = record;
		this.builtRecordVO = builtRecordVO;
	}

	public Record getRecord() {
		return record;
	}

	public RecordVO getBuiltRecordVO() {
		return builtRecordVO;
	}
}
