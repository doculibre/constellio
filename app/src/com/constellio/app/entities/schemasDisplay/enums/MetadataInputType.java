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
package com.constellio.app.entities.schemasDisplay.enums;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.schemas.MetadataValueType;

public enum MetadataInputType {

	FIELD,
	RADIO_BUTTONS,
	DROPDOWN,
	CHECKBOXES,
	TEXTAREA,
	RICHTEXT,
	LOOKUP,
	HIDDEN,
	CUSTOM,
	CONTENT,
	CONTENT_CHECK_IN_CHECK_OUT,
	URL;

	public static List<MetadataInputType> getAvailableMetadataInputTypesFor(MetadataValueType type, boolean multivalue) {
		List<MetadataInputType> inputTypes = new ArrayList<>();

		switch (type) {
		case BOOLEAN:
			break;
		case STRUCTURE:
			break;
		case ENUM:
			if (multivalue) {
				inputTypes.add(CHECKBOXES);
				inputTypes.add(DROPDOWN);
			} else {
				inputTypes.add(RADIO_BUTTONS);
				inputTypes.add(DROPDOWN);
			}
			break;
		case TEXT:
			inputTypes.add(TEXTAREA);
			inputTypes.add(RICHTEXT);
			break;
		case CONTENT:
			inputTypes.add(CONTENT);
			inputTypes.add(CONTENT_CHECK_IN_CHECK_OUT);
			break;
		case DATE:
			break;
		case DATE_TIME:
			break;
		case INTEGER:
			break;
		case REFERENCE:
			inputTypes.add(LOOKUP);
			inputTypes.add(DROPDOWN);
			if (multivalue) {
				inputTypes.add(CHECKBOXES);
			} else {
				inputTypes.add(RADIO_BUTTONS);
			}
			break;
		case STRING:
			inputTypes.add(FIELD);
			inputTypes.add(URL);
			break;
		case NUMBER:
			break;
		}

		return inputTypes;
	}

	public static String getCaptionFor(MetadataInputType type) {
		String caption = "";

		switch (type) {
		case FIELD:
			caption = "MetadataInputType.field";
			break;
		case RADIO_BUTTONS:
			caption = "MetadataInputType.radio";
			break;
		case DROPDOWN:
			caption = "MetadataInputType.dropdown";
			break;
		case CHECKBOXES:
			caption = "MetadataInputType.checkboxe";
			break;
		case TEXTAREA:
			caption = "MetadataInputType.textarea";
			break;
		case RICHTEXT:
			caption = "MetadataInputType.richtxt";
			break;
		case LOOKUP:
			caption = "MetadataInputType.lookup";
			break;
		case HIDDEN:
			caption = "MetadataInputType.hidden";
			break;
		case CUSTOM:
			caption = "MetadataInputType.custom";
			break;
		case CONTENT:
			caption = "MetadataInputType.content";
			break;
		case CONTENT_CHECK_IN_CHECK_OUT:
			caption = "MetadataInputType.contentcheck";
			break;
		case URL:
			caption = "MetadataInputType.url";
			break;
		}

		return caption;
	}

}
