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
package com.constellio.model.entities.schemas;

public enum MetadataValueType {

	//TODO Rename NUMBER to FLOATING_POINT
	DATE, DATE_TIME, STRING, TEXT, INTEGER, NUMBER, BOOLEAN, REFERENCE, CONTENT, STRUCTURE, ENUM;

	public boolean isStringOrText() {
		return this == TEXT || this == STRING;
	}

	public boolean isStructureOrContent() {
		return this == STRUCTURE || this == CONTENT;
	}

	public static String getCaptionFor(MetadataValueType type) {
		String caption = "";

		switch (type) {
		case DATE:
			caption = "MetadataValueType.date";
			break;
		case DATE_TIME:
			caption = "MetadataValueType.datetime";
			break;
		case STRING:
			caption = "MetadataValueType.string";
			break;
		case TEXT:
			caption = "MetadataValueType.text";
			break;
		case INTEGER:
			caption = "MetadataValueType.integer";
			break;
		case NUMBER:
			caption = "MetadataValueType.number";
			break;
		case BOOLEAN:
			caption = "MetadataValueType.boolean";
			break;
		case REFERENCE:
			caption = "MetadataValueType.reference";
			break;
		case CONTENT:
			caption = "MetadataValueType.content";
			break;
		case STRUCTURE:
			caption = "MetadataValueType.structure";
			break;
		case ENUM:
			caption = "MetadataValueType.enum";
			break;
		}

		return caption;
	}
}
