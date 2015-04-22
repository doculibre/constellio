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
package com.constellio.data.utils.hashing;

@SuppressWarnings("serial")
public class HashingServiceRuntimeException extends RuntimeException {

	public HashingServiceRuntimeException() {
	}

	public HashingServiceRuntimeException(String message) {
		super(message);
	}

	public HashingServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public HashingServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotGetHashFromReader extends HashingServiceRuntimeException {

		public CannotGetHashFromReader(Throwable cause) {
			super(cause);
		}
	}

	public static class CannotGetHashFromStream extends HashingServiceRuntimeException {

		public CannotGetHashFromStream(Throwable cause) {
			super(cause);
		}
	}

	public static class NoSuchAlgorithm extends HashingServiceRuntimeException {

		public NoSuchAlgorithm(String algotithm, Throwable cause) {
			super("No such algorithm " + algotithm, cause);
		}
	}

}
