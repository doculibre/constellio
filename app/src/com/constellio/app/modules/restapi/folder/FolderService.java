package com.constellio.app.modules.restapi.folder;

import com.constellio.app.modules.restapi.core.adaptor.ResourceAdaptor;
import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.service.ResourceService;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.folder.adaptor.FolderAdaptor;
import com.constellio.app.modules.restapi.folder.dao.FolderDao;
import com.constellio.app.modules.restapi.folder.dto.FolderDto;

import javax.inject.Inject;
import java.util.Set;

public class FolderService extends ResourceService {

	@Inject
	private FolderDao folderDao;
	@Inject
	private FolderAdaptor folderAdaptor;

	public FolderDto get(String host, String id, String serviceKey, String method, String date, int expiration,
						 String signature, Set<String> filters) throws Exception {
		return getResource(host, id, serviceKey, method, date, expiration, signature, filters);
	}

	@Override
	protected BaseDao getDao() {
		return folderDao;
	}

	@Override
	protected String getSchemaType() {
		return SchemaTypes.FOLDER.name();
	}

	@Override
	protected ResourceAdaptor getAdaptor() {
		return folderAdaptor;
	}
}
