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
package com.constellio.data.io.streamFactories.impl;

@SuppressWarnings("serial")
public class CopyInputStreamFactoryRuntimeException extends RuntimeException {

	private CopyInputStreamFactoryRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	private CopyInputStreamFactoryRuntimeException(String message) {
		super(message);
	}

	public static class CannotGetNewInputStreamRuntime extends CopyInputStreamFactoryRuntimeException {

		public CannotGetNewInputStreamRuntime(Exception e) {
			super("Cannot get new InputStream", e);
		}

	}

	public static class CannotWriteInputContentInAFileRuntime extends CopyInputStreamFactoryRuntimeException {

		public CannotWriteInputContentInAFileRuntime(Exception e) {
			super("Cannot write input stream content in a file", e);
		}

	}

	public static class CannotReadInputStreamRuntime extends CopyInputStreamFactoryRuntimeException {

		public CannotReadInputStreamRuntime(Exception e) {
			super("Cannot read input stream content in a file", e);
		}

	}

	public static class InputStreamIsNull extends CopyInputStreamFactoryRuntimeException {

		public InputStreamIsNull() {
			super("Input stream is null");
		}

	}
}
