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
package com.constellio.app.api.cmis;

public class ConstellioCmisException extends Exception {

	public ConstellioCmisException(String message) {
		super(message);
	}

	public ConstellioCmisException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConstellioCmisException(Throwable cause) {
		super(cause);
	}
	
	public static class ConstellioCmisException_ContentAlreadyCheckedOut extends ConstellioCmisException {
		
		public ConstellioCmisException_ContentAlreadyCheckedOut() {
			super("Cannot modify content checked out by other user");
		}
	}
	
	public static class ConstellioCmisException_IOError extends ConstellioCmisException {

		public ConstellioCmisException_IOError(Throwable cause) {
			super(cause);
		}	
	}
	
	public static class ConstellioCmisException_RecordServicesError extends ConstellioCmisException {

		public ConstellioCmisException_RecordServicesError(Throwable cause) {
			super(cause);
		}	
	}
	
	public static class ConstellioCmisException_UnsupportedVersioningState extends ConstellioCmisException {
		
		public ConstellioCmisException_UnsupportedVersioningState() {
			super("Unsupported versioning state");
		}
	}
}
