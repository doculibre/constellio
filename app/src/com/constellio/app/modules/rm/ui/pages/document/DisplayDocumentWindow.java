package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.ui.entities.RecordVO;

public class DisplayDocumentWindow extends DocumentViewWindow {

	public DisplayDocumentWindow(RecordVO recordVO) {
		this(new DisplayDocumentViewImpl(recordVO, false));
	}

	public DisplayDocumentWindow(DisplayDocumentViewImpl view) {
		super(view);
	}

}
