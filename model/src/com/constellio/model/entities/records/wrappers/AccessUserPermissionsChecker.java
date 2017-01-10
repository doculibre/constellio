package com.constellio.model.entities.records.wrappers;

import static com.constellio.model.entities.records.Record.PUBLIC_TOKEN;
import static com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils.containsAnyUserGroupTokens;
import static com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils.hasMatchingAuthorization;
import static com.constellio.model.entities.schemas.Schemas.TOKENS;
import static com.constellio.model.entities.security.Role.DELETE;
import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;

import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.SecurityTokenManager.UserTokens;

public class AccessUserPermissionsChecker extends UserPermissionsChecker {

	MetadataSchemaTypes types;

	public boolean readAccess;
	public boolean writeAccess;
	public boolean deleteAccess;

	AccessUserPermissionsChecker(User user, boolean readAccess, boolean writeAccess, boolean deleteAccess) {
		super(user);
		this.user = user;
		this.readAccess = readAccess;
		this.writeAccess = writeAccess;
		this.deleteAccess = deleteAccess;

	}

	public boolean globally() {

		boolean access = true;

		if (readAccess) {
			access &= user.hasCollectionReadAccess();
		}

		if (writeAccess) {
			access &= user.hasCollectionWriteAccess();
		}

		if (deleteAccess) {
			access &= user.hasCollectionDeleteAccess();
		}

		return access;
	}

	public boolean on(Record record) {
		boolean access = true;

		if (readAccess) {
			boolean publicRecord = record.getList(TOKENS).contains(PUBLIC_TOKEN);
			boolean globalReadAccess =
					user.hasCollectionReadAccess() || user.hasCollectionWriteAccess() || user.hasCollectionDeleteAccess();
			access = globalReadAccess || publicRecord || hasReadAccessOn(record) || hasWriteAccessOn(record) ||
					hasDeleteAccessOn(record);
		}

		if (writeAccess) {
			access &= user.hasCollectionWriteAccess() || hasWriteAccessOn(record);
		}

		if (deleteAccess) {
			access &= user.hasCollectionDeleteAccess() || hasDeleteAccessOn(record);
		}

		return access;
	}

	private boolean hasDeleteAccessOn(Record record) {
		return containsAnyUserGroupTokens(user, record, DELETE)
				|| hasMatchingAuthorization(user, record, UserAuthorizationsUtils.DELETE_ACCESS);
	}

	private boolean hasWriteAccessOn(Record record) {
		return containsAnyUserGroupTokens(user, record, WRITE)
				|| hasMatchingAuthorization(user, record, UserAuthorizationsUtils.WRITE_ACCESS);
	}

	private boolean hasReadAccessOn(Record record) {
		return containsAnyUserGroupTokens(user, record, READ)
				|| record.getList(Schemas.TOKENS).contains(PUBLIC_TOKEN)
				|| UserAuthorizationsUtils.containsAUserToken(user, record)
				|| hasMatchingAuthorization(user, record, UserAuthorizationsUtils.READ_ACCESS);
	}

	@Override
	public boolean onSomething() {
		throw new UnsupportedOperationException("onSomething() is not yet supported for this checker");
	}

}
