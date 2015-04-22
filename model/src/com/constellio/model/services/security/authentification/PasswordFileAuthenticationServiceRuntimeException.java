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
package com.constellio.model.services.security.authentification;

@SuppressWarnings("serial")
public class PasswordFileAuthenticationServiceRuntimeException extends RuntimeException {

	public PasswordFileAuthenticationServiceRuntimeException(String message) {
		super(message);
	}

	public PasswordFileAuthenticationServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class InvalidPasswordException extends PasswordFileAuthenticationServiceRuntimeException {
		public InvalidPasswordException() {
			super("Empty or not setted password");
		}
	}

	public static class IncorrectPassword extends PasswordFileAuthenticationServiceRuntimeException {
		public IncorrectPassword() {
			super("Incorrect password");
		}
	}

	public static class CannotCalculateHash extends PasswordFileAuthenticationServiceRuntimeException {

		public CannotCalculateHash(String text, Exception e) {
			super("Cannot calculate hash from " + text, e);
		}

	}
}
