package com.constellio.model.services.security.roles;

import com.constellio.model.frameworks.validation.ValidationErrors;

@SuppressWarnings("serial")
public class RolesManagerRuntimeException extends RuntimeException {
	public RolesManagerRuntimeException(String message) {
		super(message);
	}

	public RolesManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public RolesManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class RolesManagerRuntimeException_Validation extends RolesManagerRuntimeException {

		private ValidationErrors validationErrors;

		public RolesManagerRuntimeException_Validation(ValidationErrors validationErrors) {
			super(validationErrors.toErrorsSummaryString());
			this.validationErrors = validationErrors;
		}

		public ValidationErrors getValidationErrors() {
			return validationErrors;
		}
	}

	public static class RolesManagerRuntimeException_RoleNotFound extends RolesManagerRuntimeException {

		public RolesManagerRuntimeException_RoleNotFound(String roleCode) {
			super("Couldn't load a role with the code: " + roleCode);
		}
	}
}
