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
package com.constellio.model.frameworks.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationErrors {

	private final List<ValidationError> validationErrors;

	public ValidationErrors() {
		validationErrors = new ArrayList<>();
	}

	public void add(Class<?> validatorClass, String code) {
		add(validatorClass, code, new HashMap<String, String>());
	}

	public void add(Class<?> validatorClass, String code, Map<String, String> parameters) {
		validationErrors.add(new ValidationError(validatorClass.getName() + "_" + code, parameters));
	}

	public String toMultilineErrorsSummaryString() {
		StringBuilder sb = new StringBuilder();
		for (ValidationError validationError : validationErrors) {
			if (!sb.toString().isEmpty()) {
				sb.append("\n");
			}
			sb.append(validationError.toMultilineErrorSummaryString());
		}
		return sb.toString();
	}

	public String toErrorsSummaryString() {
		StringBuilder sb = new StringBuilder();
		for (ValidationError validationError : validationErrors) {
			if (!sb.toString().isEmpty()) {
				sb.append(", ");
			}
			sb.append(validationError.toErrorSummaryString());
		}
		return sb.toString();
	}

	public List<ValidationError> getValidationErrors() {
		return Collections.unmodifiableList(validationErrors);
	}
}
