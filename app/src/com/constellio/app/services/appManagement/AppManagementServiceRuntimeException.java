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
package com.constellio.app.services.appManagement;

@SuppressWarnings({ "serial" })
public class AppManagementServiceRuntimeException extends RuntimeException {

	public AppManagementServiceRuntimeException() {
	}

	public AppManagementServiceRuntimeException(String message) {
		super(message);
	}

	public AppManagementServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public AppManagementServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class WarFileNotFound extends AppManagementServiceRuntimeException {

		public WarFileNotFound() {
			super("No uploaded war files");
		}
	}

	public static class WarFileVersionMustBeHigher extends AppManagementServiceRuntimeException {

		public WarFileVersionMustBeHigher() {
			super("War file version must be higher");
		}
	}

	public static class CannotConnectToServer extends AppManagementServiceException {

		public CannotConnectToServer(String url, Exception e) {
			super("Cannot connect to server at url '" + url + "'", e);
		}

		public CannotConnectToServer(String url) {
			this("Cannot connect to server at url '" + url + "'", null);
		}
	}

}
