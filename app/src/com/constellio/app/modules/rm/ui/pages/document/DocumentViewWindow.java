package com.constellio.app.modules.rm.ui.pages.document;

import com.constellio.app.ui.framework.components.ViewWindow;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;
import com.constellio.app.ui.pages.base.BaseViewImpl;

public abstract class DocumentViewWindow extends ViewWindow {

	public DocumentViewWindow(BaseViewImpl view) throws UserDoesNotHaveAccessException {
		super(view);
		removeStyleName(WINDOW_CONTENT_SCROLL_STYLE_NAME);
	}

}
