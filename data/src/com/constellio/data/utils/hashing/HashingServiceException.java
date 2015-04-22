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
public class HashingServiceException extends Exception {

	public HashingServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public HashingServiceException(String message) {
		super(message);
	}

	public HashingServiceException(Throwable cause) {
		super(cause);
	}

	public static class Timeout extends HashingServiceException {

		public Timeout(int timeout) {
			super("Could not calculate hash in less than " + timeout + "ms");
		}

	}

	public static class CannotReadContent extends HashingServiceException {

		public CannotReadContent(Throwable cause) {
			super("Could not read content", cause);
		}

	}

	public static class CannotHashContent extends HashingServiceException {

		public CannotHashContent(Throwable cause) {
			super("Could not hash content", cause);
		}

	}

}
