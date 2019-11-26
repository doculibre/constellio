package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;

public class DisplayDocumentWindow extends DocumentViewWindow {

	public DisplayDocumentWindow(RecordVO recordVO) throws UserDoesNotHaveAccessException {
		this(new DisplayDocumentViewImpl(recordVO, false, true));
	}

	public DisplayDocumentWindow(DisplayDocumentViewImpl view) throws UserDoesNotHaveAccessException {
		super(view);
	}

}
