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
package com.constellio.data.io.streams.factories;

@SuppressWarnings({ "serial" })
public class StreamsServicesRuntimeException extends RuntimeException {

	public StreamsServicesRuntimeException() {
	}

	public StreamsServicesRuntimeException(String message) {
		super(message);
	}

	public StreamsServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public StreamsServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotCreateTempFile extends StreamsServicesRuntimeException {

		public CannotCreateTempFile(String filePath, Exception e) {
			super("Cannot create temp file " + filePath, e);
		}
	}

	public static class StreamsServicesRuntimeException_FileNotFound extends StreamsServicesRuntimeException {

		public StreamsServicesRuntimeException_FileNotFound(Exception e) {
			super(e);
		}
	}

}
