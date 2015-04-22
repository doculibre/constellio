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
package com.constellio.model.services.records.populators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.FieldsPopulator;

public class AutocompleteFieldPopulator implements FieldsPopulator {
	@Override
	public Map<String, Object> populateCopyfields(MetadataSchema schema, Record record) {

		List<String> values = new ArrayList<>();

		for (Metadata metadata : schema.getMetadatas().onlySchemaAutocomplete()) {

			if (metadata.getType().isStringOrText() && metadata.isMultivalue()) {
				values.addAll((List) record.getList(metadata));

			} else if (metadata.getType().isStringOrText() && !metadata.isMultivalue()) {
				String value = record.get(metadata);
				if (value != null) {
					values.add(value);
				}

			}

		}

		List<String> words = new ArrayList<>();

		for (String value : values) {
			if (value != null) {
				String cleanedValue = AccentApostropheCleaner.removeAccents(value).toLowerCase();
				for (String word : cleanedValue.split(" ")) {
					words.add(word);
				}
			}
		}

		Map<String, Object> fields = new HashMap<>();
		fields.put(Schemas.SCHEMA_AUTOCOMPLETE_FIELD.getDataStoreCode(), words);
		return fields;
	}

}
