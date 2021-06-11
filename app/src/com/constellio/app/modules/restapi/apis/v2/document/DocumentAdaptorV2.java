package com.constellio.app.modules.restapi.apis.v2.document;

import com.constellio.app.modules.restapi.apis.v2.core.BaseAdaptorV2;
import com.constellio.app.modules.restapi.apis.v2.core.BaseDaoV2;

import javax.inject.Inject;

public class DocumentAdaptorV2 extends BaseAdaptorV2 {

	@Inject
	private DocumentDaoV2 documentDao;

	@Override
	public BaseDaoV2 getDao() {
		return documentDao;
	}
}
