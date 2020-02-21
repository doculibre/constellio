package com.constellio.model.entities.records.wrappers;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredentialStatus;

import static com.constellio.model.entities.records.Record.GetMetadataOption.DIRECT_GET_FROM_DTO;
import static com.constellio.model.entities.records.Record.PUBLIC_TOKEN;
import static com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils.containsAnyUserGroupTokens;
import static com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils.containsNoNegativeUserGroupTokens;
import static com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils.hasMatchingAuthorizationIncludingSpecifics;
import static com.constellio.model.entities.schemas.Schemas.TOKENS;
import static com.constellio.model.entities.security.Role.DELETE;
import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;

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

		if (user.getStatus() != UserCredentialStatus.ACTIVE) {
			return false;
		}

		boolean access = true;

		if (readAccess) {
			access &= user.hasCollectionReadAccess() || user.hasCollectionWriteAccess() || user.hasCollectionDeleteAccess();
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

		if (user.getStatus() != UserCredentialStatus.ACTIVE && user.getStatus() != null) {
			return false;
		}

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

	@Override
	public boolean specificallyOn(Record record) {

		if (user.getStatus() != UserCredentialStatus.ACTIVE) {
			return false;
		}
		boolean access = true;

		if (readAccess) {
			access = hasReadAccessSpecificallyOn(record)
					 || hasWriteAccessSpecificallyOn(record) || hasDeleteAccessSpecificallyOn(record);
		}

		if (writeAccess) {
			access &= hasWriteAccessSpecificallyOn(record);
		}

		if (deleteAccess) {
			access &= hasDeleteAccessSpecificallyOn(record);
		}

		return access;
	}

	private boolean hasDeleteAccessOn(Record record) {
		return containsNoNegativeUserGroupTokens(user, record, DELETE)
			   && (containsAnyUserGroupTokens(user, record, DELETE)
				   || hasMatchingAuthorizationIncludingSpecifics(user, record, UserAuthorizationsUtils.DELETE_ACCESS)
				   || user.hasGlobalTypeAccess(record.getTypeCode(), Role.DELETE));
	}

	private boolean hasWriteAccessOn(Record record) {
		return containsNoNegativeUserGroupTokens(user, record, WRITE)
			   && (containsAnyUserGroupTokens(user, record, WRITE)
				   || hasMatchingAuthorizationIncludingSpecifics(user, record, UserAuthorizationsUtils.WRITE_ACCESS)
				   || user.hasGlobalTypeAccess(record.getTypeCode(), Role.WRITE));
	}

	private boolean hasReadAccessOn(Record record) {
		return
				containsNoNegativeUserGroupTokens(user, record, READ)
				&& (containsAnyUserGroupTokens(user, record, READ)
					|| (Toggle.PUBLIC_TOKENS.isEnabled() && record.getList(Schemas.TOKENS, DIRECT_GET_FROM_DTO).contains(PUBLIC_TOKEN))
					|| UserAuthorizationsUtils.containsAUserToken(user, record)
					|| hasMatchingAuthorizationIncludingSpecifics(user, record, UserAuthorizationsUtils.READ_ACCESS)
					|| user.hasGlobalTypeAccess(record.getTypeCode(), Role.READ));
	}

	private boolean hasDeleteAccessSpecificallyOn(Record record) {
		return containsNoNegativeUserGroupTokens(user, record, DELETE)
			   && containsAnyUserGroupTokens(user, record, DELETE);
	}

	private boolean hasWriteAccessSpecificallyOn(Record record) {
		return containsNoNegativeUserGroupTokens(user, record, WRITE)
			   && containsAnyUserGroupTokens(user, record, WRITE);
	}

	private boolean hasReadAccessSpecificallyOn(Record record) {
		return containsNoNegativeUserGroupTokens(user, record, READ)
			   && containsAnyUserGroupTokens(user, record, READ);
	}

	@Override
	public boolean onSomething() {
		throw new UnsupportedOperationException("onSomething() is not yet supported for this checker");
	}

}
