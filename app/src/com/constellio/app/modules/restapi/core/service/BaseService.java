package com.constellio.app.modules.restapi.core.service;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.exception.RecordLogicallyDeletedException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.validation.exception.UnauthenticatedUserException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserIsNotInCollection;

public abstract class BaseService {

	protected abstract BaseDao getDao();

	protected <T> T getMetadataValue(Record record, String metadataCode) {
		return getDao().getMetadataValue(record, metadataCode);
	}

	protected Record getRecord(String id, boolean checkLogicallyDeleted) {
		return getRecord(id, null, checkLogicallyDeleted);
	}

	protected Record getRecord(String id, String eTag, boolean checkLogicallyDeleted) {
		Record record = getDao().getRecordById(id, eTag);
		if (record == null) {
			throw new RecordNotFoundException(id);
		}

		if (checkLogicallyDeleted) {
			if (isLogicallyDeleted(record)) {
				throw new RecordLogicallyDeletedException(id);
			}
		}

		return record;
	}

	protected User getUserByServiceKey(String serviceKey, String collection) {
		User user = getDao().getUser(serviceKey, collection);
		if (user == null) {
			throw new UnauthenticatedUserException();
		}

		return user;
	}

	protected User getUserByUsername(String username, String collection) {
		try {
			User user = getDao().getUserByUsername(username, collection);
			if (user == null) {
				throw new UnauthenticatedUserException();
			}
			return user;
		} catch (UserServicesRuntimeException_NoSuchUser | UserServicesRuntimeException_UserIsNotInCollection e) {
			throw new RecordNotFoundException(username);
		}
	}

	protected boolean isLogicallyDeleted(Record record) {
		return (Boolean.TRUE.equals(record.get(Schemas.LOGICALLY_DELETED_STATUS)));
	}

}
