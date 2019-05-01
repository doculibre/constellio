package com.constellio.app.modules.restapi.folder.dao;

import com.constellio.app.modules.restapi.folder.dto.FolderDto;
import com.constellio.app.modules.restapi.resource.dao.ResourceDao;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;

public class FolderDao extends ResourceDao {

	@Override
	protected String getSchemaType() {
		return Folder.SCHEMA_TYPE;
	}

	public Record createFolder(User user, MetadataSchema folderSchema, FolderDto folderDto, String flush)
			throws Exception {
		// TODO
		return null;
	}


}
