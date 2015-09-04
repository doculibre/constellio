/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
