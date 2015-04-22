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
package com.constellio.model.services.parser;

@SuppressWarnings("serial")
public class FileParserException extends Exception {

	private FileParserException(String message, Throwable cause) {
		super(message, cause);
	}

	private FileParserException(String message) {
		super(message);
	}

	public static class FileParserException_CannotParse extends FileParserException {

		private String detectedMimetype;

		public FileParserException_CannotParse(Exception e, String detectedMimetype) {
			super("Cannot parse file", e);
			this.detectedMimetype = detectedMimetype;
		}

		public String getDetectedMimetype() {
			return detectedMimetype;
		}
	}
}
