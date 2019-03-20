package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.ui.entities.RecordVO;

public class AddEditDocumentWindow extends DocumentViewWindow {

	public AddEditDocumentWindow(RecordVO recordVO) {
		this(new AddEditDocumentViewImpl(recordVO));
	}

	public AddEditDocumentWindow(AddEditDocumentViewImpl view) {
		super(view);
	}

}
