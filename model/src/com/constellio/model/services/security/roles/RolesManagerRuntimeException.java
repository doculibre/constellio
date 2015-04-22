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
