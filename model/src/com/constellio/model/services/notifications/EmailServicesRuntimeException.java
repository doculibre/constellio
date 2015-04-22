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
package com.constellio.model.services.notifications;

@SuppressWarnings("serial")
public class EmailServicesRuntimeException extends RuntimeException {

	public EmailServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class EmailServicesRuntimeException_CannotSendEmail extends EmailServicesRuntimeException {

		public EmailServicesRuntimeException_CannotSendEmail(Throwable cause) {
			super("Cannot send email", cause);
		}
	}

	public static class EmailServicesRuntimeException_UnsupportedEncodingException extends EmailServicesRuntimeException {

		public EmailServicesRuntimeException_UnsupportedEncodingException(Throwable cause) {
			super("Unsupported encoding", cause);
		}
	}

	public static class EmailServicesRuntimeException_MessagingException extends EmailServicesRuntimeException {

		public EmailServicesRuntimeException_MessagingException(Throwable cause) {
			super("Cannot connect", cause);
		}
	}

	public static class EmailServicesRuntimeException_CannotGetStore extends EmailServicesRuntimeException {

		public EmailServicesRuntimeException_CannotGetStore(String store, Throwable cause) {
			super("Cannot get store: " + store, cause);
		}
	}
}
