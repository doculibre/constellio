package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.ui.entities.RecordVO;

public class DisplayDocumentWindow extends DocumentViewWindow {

	public DisplayDocumentWindow(RecordVO recordVO) {
		this(new DisplayDocumentViewImpl(recordVO, false, true));
	}

	public DisplayDocumentWindow(DisplayDocumentViewImpl view) {
		super(view);
	}

	@Override
	protected String getViewHeight() {
		DisplayDocumentViewImpl view = (DisplayDocumentViewImpl) getView();
		RecordVO recordVO = view.getRecordVO();
		int metadataCount = recordVO.getMetadatas().size();
		int viewHeight = 230 + (20 * metadataCount);
		return viewHeight + "px";
	}

}
