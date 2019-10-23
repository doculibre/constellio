package com.constellio.app.ui.pages.management.schemaRecords;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;

public class AddEditSchemaRecordWindow extends SchemaRecordViewWindow {

	public AddEditSchemaRecordWindow(RecordVO recordVO, String schemaType) throws UserDoesNotHaveAccessException {
		this(new AddEditSchemaRecordViewImpl(recordVO, schemaType));
	}

	public AddEditSchemaRecordWindow(AddEditSchemaRecordViewImpl view) throws UserDoesNotHaveAccessException {
		super(view);
	}
}
