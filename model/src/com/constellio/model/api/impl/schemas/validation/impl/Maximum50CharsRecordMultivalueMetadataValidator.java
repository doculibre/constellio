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
package com.constellio.model.api.impl.schemas.validation.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class Maximum50CharsRecordMultivalueMetadataValidator implements RecordMetadataValidator<List<String>> {

	public static final String WAS_SIZE = "wasSize";
	public static final String MAX_SIZE = "maxSize";
	public static final String VALUE_LENGTH_TOO_LONG = "valueLengthTooLong";

	@Override
	public void validate(Metadata metadata, List<String> values, ConfigProvider configProvider, ValidationErrors validationErrors) {
		for (String value : values) {
			if (value != null && value.length() > 50) {
				Map<String, String> parameters = new HashMap<>();
				parameters.put(MAX_SIZE, "50");
				parameters.put(WAS_SIZE, "" + value.length());
				validationErrors.add(getClass(), VALUE_LENGTH_TOO_LONG, parameters);
			}
		}
	}
}
