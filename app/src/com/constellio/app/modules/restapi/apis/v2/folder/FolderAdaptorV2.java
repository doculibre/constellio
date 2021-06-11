package com.constellio.app.modules.restapi.apis.v2.folder;

import com.constellio.app.modules.restapi.apis.v2.core.BaseAdaptorV2;
import com.constellio.app.modules.restapi.apis.v2.core.BaseDaoV2;

import javax.inject.Inject;

public class FolderAdaptorV2 extends BaseAdaptorV2 {

	@Inject
	private FolderDaoV2 folderDao;

	@Override
	public BaseDaoV2 getDao() {
		return folderDao;
	}
}
