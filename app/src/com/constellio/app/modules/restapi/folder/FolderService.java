package com.constellio.app.modules.restapi.folder;

import com.constellio.app.modules.restapi.ace.AceService;
import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.folder.adaptor.FolderAdaptor;
import com.constellio.app.modules.restapi.folder.dao.FolderDao;
import com.constellio.app.modules.restapi.folder.dto.FolderDto;
import com.constellio.app.modules.restapi.resource.adaptor.ResourceAdaptor;
import com.constellio.app.modules.restapi.resource.service.ResourceService;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;

import javax.inject.Inject;
import java.util.Set;

import static com.constellio.app.modules.restapi.core.util.ListUtils.isNullOrEmpty;

public class FolderService extends ResourceService {

	@Inject
	private FolderDao folderDao;
	@Inject
	private FolderAdaptor folderAdaptor;
	@Inject
	private AceService aceService;

	public FolderDto get(String host, String id, String serviceKey, String method, String date, int expiration,
						 String signature, Set<String> filters) throws Exception {
		return getResource(host, id, serviceKey, method, date, expiration, signature, filters);
	}

	public FolderDto create(String host, String parentFolderId, String serviceKey, String method, String date,
							int expiration, String signature, FolderDto folderDto, String sourceFolderId,
							String flushMode, Set<String> filters) throws Exception {
		validateParameters(host, parentFolderId, serviceKey, method, date, expiration, null, null, signature);

		Record folder = getRecord(parentFolderId != null ? parentFolderId : sourceFolderId, true);
		String collection = folder.getCollection();
		User user = getUser(serviceKey, collection);
		validateUserAccess(user, folder, method);

		// TODO validate access on admin unit? category?

		MetadataSchema folderSchema = folderDao.getLinkedMetadataSchema(folderDto.getType(), collection);
		validateExtendedAttributes(folderDto.getExtendedAttributes(), folderSchema);
		validateAuthorizations(folderDto.getDirectAces(), folder.getCollection());

		boolean acesModified = false;
		Record createdFolderRecord = folderDao.createFolder(user, folderSchema, folderDto, flushMode);
		if (!isNullOrEmpty(folderDto.getDirectAces())) {
			aceService.addAces(user, createdFolderRecord, folderDto.getDirectAces());
			acesModified = true;
		}
		return getAdaptor().adapt(folderDto, createdFolderRecord, folderSchema, acesModified, filters);
	}

	@Override
	protected BaseDao getDao() {
		return folderDao;
	}

	@Override
	protected SchemaTypes getSchemaType() {
		return SchemaTypes.FOLDER;
	}

	@Override
	protected ResourceAdaptor<FolderDto> getAdaptor() {
		return folderAdaptor;
	}
}
