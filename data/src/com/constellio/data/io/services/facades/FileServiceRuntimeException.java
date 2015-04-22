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
package com.constellio.data.io.services.facades;

@SuppressWarnings("serial")
public class FileServiceRuntimeException extends RuntimeException {

	public FileServiceRuntimeException() {
	}

	public FileServiceRuntimeException(String message) {
		super(message);
	}

	public FileServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public FileServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotCopyFile extends FileServiceRuntimeException {

		public CannotCopyFile(String src, String dest, Throwable t) {
			super("Cannot copy '" + src + "' to '" + dest + "'", t);
		}
	}

	public static class CannotDeleteFile extends FileServiceRuntimeException {

		public CannotDeleteFile(String filePath, Throwable t) {
			super("Cannot delete '" + filePath + "'", t);
		}
	}

	public static class CannotCreateTemporaryFolder extends FileServiceRuntimeException {

		public CannotCreateTemporaryFolder(Throwable t) {
			super("Cannot create temporary folder", t);
		}

		public CannotCreateTemporaryFolder() {
			super("Cannot create temporary folder");
		}
	}

	public static class CannotReadStreamToString extends FileServiceRuntimeException {

		public CannotReadStreamToString(Throwable t) {
			super("Cannot read stream to string", t);
		}
	}

	public static class FileServiceRuntimeException_CannotReadFile extends FileServiceRuntimeException {

		public FileServiceRuntimeException_CannotReadFile(String filePath, Throwable t) {
			super("Cannot read file '" + filePath + "'", t);
		}
	}

}
