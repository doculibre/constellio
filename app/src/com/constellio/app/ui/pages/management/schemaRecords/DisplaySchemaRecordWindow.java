package com.constellio.app.ui.pages.management.schemaRecords;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;

public class DisplaySchemaRecordWindow extends SchemaRecordViewWindow {

	public DisplaySchemaRecordWindow(RecordVO recordVO) throws UserDoesNotHaveAccessException {
		this(new DisplaySchemaRecordViewImpl(recordVO, false, true, true));
	}

	public DisplaySchemaRecordWindow(DisplaySchemaRecordViewImpl view) throws UserDoesNotHaveAccessException {
		super(view);
	}

}
