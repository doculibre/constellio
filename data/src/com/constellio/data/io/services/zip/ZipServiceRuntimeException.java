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
package com.constellio.data.io.services.zip;

@SuppressWarnings("serial")
public class ZipServiceRuntimeException extends RuntimeException {

	public ZipServiceRuntimeException() {
	}

	public ZipServiceRuntimeException(String message) {
		super(message);
	}

	public ZipServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public ZipServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotUnzip extends ZipServiceRuntimeException {

		public CannotUnzip(String zipFilePath, String zipFileContentDestinationDirPath, Throwable t) {
			super("Cannot file: '" + zipFilePath + "' in '" + zipFileContentDestinationDirPath + "'", t);
		}
	}

}
