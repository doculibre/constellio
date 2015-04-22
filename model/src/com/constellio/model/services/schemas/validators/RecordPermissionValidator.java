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
package com.constellio.model.services.schemas.validators;

import java.util.HashMap;
import java.util.Map;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.services.security.AuthorizationsServices;

public class RecordPermissionValidator implements Validator<Record> {

	public static final String UNAUTHORIZED = "userHasNoWriteAccess";
	public static final String RECORD_ID = "recordId";
	public static final String USERNAME = "username";

	private Transaction transaction;
	private AuthorizationsServices authorizationsServices;

	public RecordPermissionValidator(Transaction transaction, AuthorizationsServices authorizationsServices) {
		this.transaction = transaction;
		this.authorizationsServices = authorizationsServices;
	}

	@Override
	public void validate(Record record, ValidationErrors validationErrors) {
		if (transaction.getUser() != null && record.isDirty() && !record.isModified(Schemas.LOGICALLY_DELETED_STATUS)) {
			if (!authorizationsServices.canWrite(transaction.getUser(), record)) {
				addValidationErrors(validationErrors, UNAUTHORIZED, record, transaction.getUser());
			}
		}
	}

	private void addValidationErrors(ValidationErrors validationErrors, String errorCode, Record record, User user) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(RECORD_ID, record.getId());
		parameters.put(USERNAME, user.getUsername());
		validationErrors.add(getClass(), errorCode, parameters);
	}
}