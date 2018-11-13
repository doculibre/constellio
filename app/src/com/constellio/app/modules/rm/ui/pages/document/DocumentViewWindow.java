package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.ui.framework.components.ViewWindow;
import com.constellio.app.ui.pages.base.BaseViewImpl;

public class DocumentViewWindow extends ViewWindow {

	public DocumentViewWindow(BaseViewImpl view) {
		super(view);
		removeStyleName(WINDOW_CONTENT_SCROLL_STYLE_NAME);
	}

}
