package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.ui.framework.components.ViewWindow;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;
import com.constellio.app.ui.pages.base.BaseViewImpl;

public class FolderViewWindow extends ViewWindow {

	public FolderViewWindow(BaseViewImpl view) throws UserDoesNotHaveAccessException {
		super(view);
		removeStyleName(WINDOW_CONTENT_SCROLL_STYLE_NAME);
	}

}
