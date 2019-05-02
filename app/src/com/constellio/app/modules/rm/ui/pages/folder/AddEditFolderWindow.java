package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.ui.entities.RecordVO;

public class AddEditFolderWindow extends FolderViewWindow {

	public AddEditFolderWindow(RecordVO recordVO) {
		this(new AddEditFolderViewImpl(recordVO));
	}

	public AddEditFolderWindow(AddEditFolderViewImpl view) {
		super(view);
	}

}
