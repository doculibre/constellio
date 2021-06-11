package com.constellio.app.modules.restapi.apis.v2.folder;

import com.constellio.app.modules.restapi.apis.v2.core.BaseDaoV2;
import com.constellio.app.modules.restapi.apis.v2.core.BaseServiceV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FilterMode;
import com.constellio.app.modules.restapi.apis.v2.record.dto.RecordDtoV2;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

import javax.inject.Inject;

public class FolderServiceV2 extends BaseServiceV2 {

	@Inject
	private FolderDaoV2 folderDao;
	@Inject
	private FolderAdaptorV2 folderAdaptor;

	public RecordDtoV2 get(String id, FilterMode filterMode, String token, String host, String eTag) throws Exception {
		validateHost(host);

		Record record = getRecord(id, eTag, filterMode);
		User user = getUserByToken(token, record.getCollection());
		validateUserAccess(user, record, true, false, false);

		return folderAdaptor.adaptRecord(record);
	}

	@Override
	protected BaseDaoV2 getDao() {
		return folderDao;
	}

}
