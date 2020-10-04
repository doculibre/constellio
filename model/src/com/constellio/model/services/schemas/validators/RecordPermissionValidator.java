package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.services.security.AuthorizationsServices;

import java.util.HashMap;
import java.util.Map;

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
	public void validate(Record record, ValidationErrors validationErrors, boolean skipNonEssential) {
		if (transaction.getUser() != null && record.isDirty()
			&& !(record.isModified(Schemas.LOGICALLY_DELETED_STATUS) || record.isModified(Schemas.LOGICALLY_DELETED_ON))) {
			if (!authorizationsServices.canWrite(transaction.getUser(), record)) {
				addValidationErrors(validationErrors, UNAUTHORIZED, record, transaction.getUser());
			}
		}
	}

	private void addValidationErrors(ValidationErrors validationErrors, String errorCode, Record record, User user) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(RECORD_ID, record.getId());
		parameters.put(USERNAME, user.getUsername());
		validationErrors.add(getClass(), errorCode, parameters);
	}
}