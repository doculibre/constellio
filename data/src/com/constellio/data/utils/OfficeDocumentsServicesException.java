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

@SuppressWarnings("serial")
public class OfficeDocumentsServicesException extends Exception {

	public OfficeDocumentsServicesException(String message, Exception e) {
		super(message, e);
	}

	public OfficeDocumentsServicesException(String message) {
		super(message);
	}

	public static class CannotReadDocumentsProperties extends OfficeDocumentsServicesException {

		public CannotReadDocumentsProperties(Exception e) {
			super("Cannot read documents properties", e);
		}

	}

	public static class PropertyDoesntExist extends OfficeDocumentsServicesException {

		public PropertyDoesntExist(String propertyName) {
			super("The property doesn't exists : " + propertyName);
		}
	}

	public static class NotCompatibleExtension extends OfficeDocumentsServicesException {
		public NotCompatibleExtension(String ext) {
			super("The extension is not compatible : " + ext);
		}
	}

	public static class RTFFileIsNotCompatible extends OfficeDocumentsServicesException {
		public RTFFileIsNotCompatible() {
			super("The file is a RTF Document");
		}
	}

}
