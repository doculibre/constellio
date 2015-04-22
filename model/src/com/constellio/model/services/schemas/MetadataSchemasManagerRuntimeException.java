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
package com.constellio.model.services.schemas;

@SuppressWarnings("serial")
public class MetadataSchemasManagerRuntimeException extends RuntimeException {

	public MetadataSchemasManagerRuntimeException() {
	}

	public MetadataSchemasManagerRuntimeException(String message) {
		super(message);
	}

	public MetadataSchemasManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public MetadataSchemasManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class NoSuchValidatorClass extends MetadataSchemasManagerRuntimeException {

		public NoSuchValidatorClass(String validatorClassName, Exception e) {
			super("No such validator class : " + validatorClassName, e);
		}
	}

	public static class CannotUpdateDocument extends MetadataSchemasManagerRuntimeException {

		public CannotUpdateDocument(String document, Exception e) {
			super("Cannot update document : " + document, e);
		}
	}

}
