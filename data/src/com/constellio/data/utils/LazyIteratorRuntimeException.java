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
package com.constellio.data.utils;

public class LazyIteratorRuntimeException extends RuntimeException {

	public LazyIteratorRuntimeException(String message) {
		super(message);
	}

	public LazyIteratorRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public LazyIteratorRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class LazyIteratorRuntimeException_IncorrectUsage extends LazyIteratorRuntimeException {

		public LazyIteratorRuntimeException_IncorrectUsage() {
			super("Cannot call next, since there is no more results");
		}

	}

	public static class LazyIteratorRuntimeException_RemoveNotAvailable extends LazyIteratorRuntimeException {

		public LazyIteratorRuntimeException_RemoveNotAvailable() {
			super("Remove is not available");
		}

	}
}
