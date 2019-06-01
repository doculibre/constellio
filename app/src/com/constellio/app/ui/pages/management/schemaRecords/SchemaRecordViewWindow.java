package com.constellio.app.ui.pages.management.schemaRecords;

import com.constellio.app.ui.framework.components.ViewWindow;
import com.constellio.app.ui.pages.base.BaseViewImpl;

public class SchemaRecordViewWindow extends ViewWindow {

	public SchemaRecordViewWindow(BaseViewImpl view) {
		super(view);
		removeStyleName(WINDOW_CONTENT_SCROLL_STYLE_NAME);
	}

}
