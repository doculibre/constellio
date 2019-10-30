package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;

public class DisplayFolderWindow extends FolderViewWindow {

	public DisplayFolderWindow(RecordVO recordVO) throws UserDoesNotHaveAccessException {
		this(new DisplayFolderViewImpl(recordVO, false, true));
	}

	public DisplayFolderWindow(DisplayFolderViewImpl view) throws UserDoesNotHaveAccessException {
		super(view);
	}

}
