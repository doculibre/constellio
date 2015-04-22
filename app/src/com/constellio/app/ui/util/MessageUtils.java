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
