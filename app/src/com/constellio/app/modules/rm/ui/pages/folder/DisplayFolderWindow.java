package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.ui.entities.RecordVO;

public class DisplayFolderWindow extends FolderViewWindow {

	public DisplayFolderWindow(RecordVO recordVO) {
		this(new DisplayFolderViewImpl(recordVO, false, true));
	}

	public DisplayFolderWindow(DisplayFolderViewImpl view) {
		super(view);
	}

}
