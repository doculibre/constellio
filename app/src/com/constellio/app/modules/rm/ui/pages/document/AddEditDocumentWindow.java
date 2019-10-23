package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;

public class AddEditDocumentWindow extends DocumentViewWindow {

	public AddEditDocumentWindow(RecordVO recordVO) throws UserDoesNotHaveAccessException {
		this(new AddEditDocumentViewImpl(recordVO));
	}

	public AddEditDocumentWindow(AddEditDocumentViewImpl view) throws UserDoesNotHaveAccessException {
		super(view);
	}

}
