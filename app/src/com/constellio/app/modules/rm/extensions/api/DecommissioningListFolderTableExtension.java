package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.schemas.Metadata;

import java.io.Serializable;

public interface DecommissioningListFolderTableExtension extends Serializable {
	
	String getPreviousId(FolderDetailVO detail);

	String getPreviousIdOrNull(FolderDetailVO detail);

	String getPreviousId(Folder folder);

	void addPreviousIdToFolderVO(FolderDetailVO folderVO);

	Metadata getPreviousIdMetadata();
}
