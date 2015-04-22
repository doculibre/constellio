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
package com.constellio.model.services.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.security.Role;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;

public class RoleValidator implements Validator<Role> {
	public static final String INVALID_CODE = "invalidCode";
	public static final String NON_UNIQUE_CODE = "codeNotUnique";
	public static final String NOT_FOUND = "roleNotFoundForCode";
	public static final String EMPTY_CODE = "codeCannotBeEmpty";

	private List<Role> roles;
	private boolean updateValidation;

	public RoleValidator(List<Role> roles, boolean updateValidation) {
		this.roles = roles;
		this.updateValidation = updateValidation;
		if (roles == null) {
			throw new ImpossibleRuntimeException("Roles required");
		}
	}

	@Override
	public void validate(Role role, ValidationErrors validationErrors) {
		this.validate(role.getCode(), validationErrors);
	}

	public void validate(String code, ValidationErrors validationErrors) {
		if (code.isEmpty()) {
			addValidationErrors(validationErrors, EMPTY_CODE, code);
		}

		boolean found = false;
		for (Role role : roles) {
			if (role.getCode().equals(code) && !updateValidation) {
				addValidationErrors(validationErrors, NON_UNIQUE_CODE, code);
				break;
			} else if (role.getCode().equals(code) && updateValidation) {
				found = true;
			}
		}

		if (updateValidation && !found) {
			addValidationErrors(validationErrors, NOT_FOUND, code);
		}

	}

	private void addValidationErrors(ValidationErrors validationErrors, String errorCode, String invalidCode) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(INVALID_CODE, invalidCode);
		validationErrors.add(getClass(), errorCode, parameters);
	}
}
