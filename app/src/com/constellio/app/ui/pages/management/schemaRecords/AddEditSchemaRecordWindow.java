package com.constellio.app.ui.pages.management.schemaRecords;

import com.constellio.app.ui.entities.RecordVO;

public class AddEditSchemaRecordWindow extends SchemaRecordViewWindow {

	public AddEditSchemaRecordWindow(RecordVO recordVO) {
		this(new AddEditSchemaRecordViewImpl(recordVO));
	}

	public AddEditSchemaRecordWindow(AddEditSchemaRecordViewImpl view) {
		super(view);
	}

}
