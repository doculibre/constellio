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

import java.io.File;

@SuppressWarnings("serial")
public class CustomPluginsPackagingServiceException extends RuntimeException {

	public CustomPluginsPackagingServiceException(String message, Exception e) {
		super(message, e);
	}

	public CustomPluginsPackagingServiceException(String message) {
		super(message);
	}

	public static class MethodCannotBeParsed extends CustomPluginsPackagingServiceException {

		public MethodCannotBeParsed(File license, String method) {
			super("Method '" + method + "' of license '" + license.getAbsolutePath() + "' cannot be parsed");
		}

	}

	public static class InvalidDate extends CustomPluginsPackagingServiceException {

		public InvalidDate(File license, String method, IllegalArgumentException e) {
			super("Date value of method '" + method + "' of license '" + license.getAbsolutePath() + "' cannot be parsed", e);
		}

	}

}
