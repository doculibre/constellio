package com.constellio.app.servlet;

import com.constellio.app.modules.restapi.apis.v1.validation.exception.ExpiredTokenException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.core.exception.RecordLogicallyDeletedException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserIsNotInCollection;
import org.joda.time.LocalDateTime;

import java.util.Map;

public abstract class BaseServletService {

	protected abstract BaseServletDao getDao();

	protected void validateToken(String token, String serviceKey) {
		Map<String, LocalDateTime> tokens = getDao().getUserAccessTokens(serviceKey);
		if (!tokens.containsKey(token)) {
			throw new UnauthenticatedUserException();
		}

		if (tokens.get(token).isBefore(TimeProvider.getLocalDateTime())) {
			throw new ExpiredTokenException();
		}
	}

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

	protected String getUsernameByServiceKey(String serviceKey) {
		String username = getDao().getUsernameByServiceKey(serviceKey);
		if (username == null) {
			throw new UnauthenticatedUserException();
		}

		return username;
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
