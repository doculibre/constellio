package com.constellio.app.extensions.records.params;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;

public class GetRecordsToSaveInSameTransactionAsParentRecordParams {
	private final RecordVO parentRecordSaved;
	private final RecordForm form;

	public GetRecordsToSaveInSameTransactionAsParentRecordParams(
			RecordVO parentRecordSaved,
			RecordForm form) {
		this.parentRecordSaved = parentRecordSaved;
		this.form = form;
	}

	public RecordVO getParentRecordSaved() {
		return parentRecordSaved;
	}

	public RecordForm getForm() {
		return form;
	}
}
