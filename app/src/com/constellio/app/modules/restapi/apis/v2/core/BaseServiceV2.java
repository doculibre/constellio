package com.constellio.app.modules.restapi.apis.v2.core;

import com.constellio.app.modules.restapi.apis.v1.core.BaseService;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnallowedHostException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FilterMode;
import com.constellio.app.modules.restapi.core.exception.CollectionNotFoundException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public abstract class BaseServiceV2 extends BaseService {

	protected abstract BaseDaoV2 getDao();

	protected Record getRecord(String id, String eTag, FilterMode filterMode) {
		Record record = getDao().getRecordById(id, eTag, filterMode);
		if (record == null) {
			throw new RecordNotFoundException(id);
		}

		return record;
	}

	protected User getUserByToken(String token, String collection) {
		User user = getDao().getUserByToken(token, collection);
		if (user == null) {
			throw new UnauthenticatedUserException();
		}

		return user;
	}

	protected void validateCollection(String collection) {
		boolean exist = getDao().isExistingCollection(collection);
		if (!exist) {
			throw new CollectionNotFoundException(collection);
		}
	}

	protected void validateSchemaType(String collection, String schemaType) {
		getDao().getMetadataSchemaType(collection, schemaType);
	}

	protected void validateHost(String host) {
		if (!getDao().getAllowedHosts().contains(host)) {
			throw new UnallowedHostException(host);
		}
	}

	protected void validateUserAccess(User user, Record record, boolean read, boolean write, boolean delete) {
		if (read && !user.hasReadAccess().on(record)) {
			throw new UnauthorizedAccessException();
		}
		if (write && !user.hasWriteAccess().on(record)) {
			throw new UnauthorizedAccessException();
		}
		if (delete && !user.hasDeleteAccess().on(record)) {
			throw new UnauthorizedAccessException();
		}
	}

}
