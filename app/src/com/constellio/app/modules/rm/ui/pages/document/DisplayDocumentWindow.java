package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.ui.entities.RecordVO;

public class DisplayDocumentWindow extends DocumentViewWindow {

	public DisplayDocumentWindow(RecordVO recordVO) {
		this(new DisplayDocumentViewImpl(recordVO, false, true));
	}

	public DisplayDocumentWindow(DisplayDocumentViewImpl view) {
		super(view);
	}

}
