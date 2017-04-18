package com.constellio.app.modules.rm.extensions.api;

import java.io.Serializable;

import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;

public interface DecommissioningListFolderTableExtension extends Serializable {
	
	String getPreviousId(FolderDetailVO detail);

}
