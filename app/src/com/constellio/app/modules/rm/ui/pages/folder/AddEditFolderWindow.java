package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;

public class AddEditFolderWindow extends FolderViewWindow {

	public AddEditFolderWindow(RecordVO recordVO) throws UserDoesNotHaveAccessException {
		this(new AddEditFolderViewImpl(recordVO));
	}

	public AddEditFolderWindow(AddEditFolderViewImpl view) throws UserDoesNotHaveAccessException {
		super(view);
	}

}
