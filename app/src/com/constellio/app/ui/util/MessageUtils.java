package com.constellio.app.ui.util;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord;

public class MessageUtils {

	public static String toMessage(Exception e) {

		ValidationErrors errors = getValidationErrors(e);

		if (errors != null) {
			return toMessage(errors);

		} else if (e instanceof RecordServicesRuntimeException_CannotLogicallyDeleteRecord) {
			return $("cannotLogicallyDelete");

		} else if (e instanceof RecordServicesRuntimeException_CannotPhysicallyDeleteRecord) {
			return $("cannotPhysicallyDelete");

		} else {
			e.printStackTrace();
			return toGeneralErrorMessageWithText(e.getMessage());
		}

	}

	public static ValidationErrors getValidationErrors(Throwable e) {
		if (e instanceof ValidationException) {
			return ((ValidationException) e).getValidationErrors();

		} else if (e instanceof RecordServicesException.ValidationException) {
			return ((RecordServicesException.ValidationException) e).getErrors();

		} else if (e instanceof ValidationRuntimeException) {
			return ((ValidationRuntimeException) e).getValidationErrors();

		} else if (e.getCause() != null) {
			return getValidationErrors(e.getCause());
		} else {
			return null;
		}
	}

	private static String toGeneralErrorMessageWithText(String message) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append($("runtimeException"));
		stringBuilder.append("\n\n");
		stringBuilder.append(message);

		return stringBuilder.toString();

	}

	private static String toMessage(ValidationErrors validationErrors) {

		StringBuilder stringBuilder = new StringBuilder();

		for (ValidationError validationError : validationErrors.getValidationErrors()) {
			if (stringBuilder.length() != 0) {
				stringBuilder.append("\n");
			}
			stringBuilder.append($(validationError));

		}

		return stringBuilder.toString();

	}

}
