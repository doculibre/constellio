package com.constellio.model.entities.records.wrappers;

import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;

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
			boolean publicRecord = record.getList(Schemas.TOKENS).contains(Record.PUBLIC_TOKEN);
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

		List<String> userTokens = user.getUserTokens();
		List<String> recordTokens = record.getList(Schemas.TOKENS);
		for (String token : recordTokens) {
			if (token.startsWith("d") && userTokens.contains(token)) {
				return true;
			}
		}

		return false;
	}

	private boolean hasWriteAccessOn(Record record) {

		List<String> userTokens = user.getUserTokens();
		List<String> recordTokens = record.getList(Schemas.TOKENS);
		for (String token : recordTokens) {
			if (token.startsWith("w") && userTokens.contains(token)) {
				return true;
			}
		}

		return false;
	}

	private boolean hasReadAccessOn(Record record) {

		List<String> userTokens = user.getUserTokens();
		List<String> recordTokens = record.getList(Schemas.TOKENS);
		for (String token : recordTokens) {
			if (token.startsWith("r") && userTokens.contains(token)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean onSomething() {
		throw new UnsupportedOperationException("onSomething() is not yet supported for this checker");
	}

}
