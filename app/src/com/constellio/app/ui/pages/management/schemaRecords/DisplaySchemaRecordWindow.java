package com.constellio.app.ui.pages.management.schemaRecords;

import com.constellio.app.ui.entities.RecordVO;

public class DisplaySchemaRecordWindow extends SchemaRecordViewWindow {

	public DisplaySchemaRecordWindow(RecordVO recordVO) {
		this(new DisplaySchemaRecordViewImpl(recordVO, false, true));
	}

	public DisplaySchemaRecordWindow(DisplaySchemaRecordViewImpl view) {
		super(view);
	}

}
