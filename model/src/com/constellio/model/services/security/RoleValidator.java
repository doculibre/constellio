package com.constellio.model.services.security;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.security.Role;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public boolean isEssential() {
		return true;
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
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(INVALID_CODE, invalidCode);
		validationErrors.add(getClass(), errorCode, parameters);
	}
}
