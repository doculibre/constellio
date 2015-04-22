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
package com.constellio.model.packaging.custom;

@SuppressWarnings("serial")
public class CustomPluginsPackagingServiceRuntimeException extends RuntimeException {

	public CustomPluginsPackagingServiceRuntimeException() {
	}

	public CustomPluginsPackagingServiceRuntimeException(String message) {
		super(message);
	}

	public CustomPluginsPackagingServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public CustomPluginsPackagingServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotWriteCustumerLicense extends CustomPluginsPackagingServiceRuntimeException {

		public CannotWriteCustumerLicense(String custumerName, String tempFilePath, Exception e) {
			super("Cannot write custumer license. Customer : " + custumerName + ", temporay file : " + tempFilePath, e);
		}
	}

	public static class CannotBuildCustumerJar extends CustomPluginsPackagingServiceRuntimeException {

		public CannotBuildCustumerJar(String custumerName, String binFolder, String jarDestinationFolder, Exception e) {
			super("Cannot build a custumer jar : " + custumerName + ", bin folder : " + binFolder + ", jar destination folder : "
					+ jarDestinationFolder, e);
		}

	}

}
