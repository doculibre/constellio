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
package com.constellio.model.services.contents;

@SuppressWarnings("serial")
public class ContentManagerRuntimeException extends RuntimeException {

	public ContentManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContentManagerRuntimeException(String message) {
		super(message);
	}

	public ContentManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ContentManagerRuntimeException_CannotReadInputStream extends ContentManagerRuntimeException {

		public ContentManagerRuntimeException_CannotReadInputStream(Throwable cause) {
			super("Cannot read content", cause);
		}

	}

	public static class ContentManagerRuntimeException_CannotSaveContent extends ContentManagerRuntimeException {

		public ContentManagerRuntimeException_CannotSaveContent(Throwable cause) {
			super("Cannot save content", cause);
		}

	}

	public static class ContentManagerRuntimeException_NoSuchContent extends ContentManagerRuntimeException {
		public ContentManagerRuntimeException_NoSuchContent(String id) {
			super("No such content for id '" + id + "'");
		}

		public ContentManagerRuntimeException_NoSuchContent(String id, Throwable cause) {
			super("No such content for id '" + id + "'", cause);
		}
	}

}
