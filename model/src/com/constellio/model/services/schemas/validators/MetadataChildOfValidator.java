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
package com.constellio.model.services.schemas.validators;

import java.util.HashMap;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.services.records.RecordImplRuntimeException.RecordImplException_RecordCannotHaveTwoParents;

public class MetadataChildOfValidator implements Validator<Record> {

	public static final String MULTIPLE_PARENTS = "multipleParentForRecord";

	private final List<Metadata> metadatas;
	private final MetadataSchemaTypes schemaTypes;

	public MetadataChildOfValidator(List<Metadata> metadatas, MetadataSchemaTypes schemaTypes) {
		this.metadatas = metadatas;
		this.schemaTypes = schemaTypes;
	}

	@Override
	public void validate(Record record, ValidationErrors validationErrors) {
		List<Metadata> parentMetadatas = schemaTypes.getSchema(record.getSchemaCode()).getParentReferences();
		if (!parentMetadatas.isEmpty()) {
			try {
				record.getNonNullValueIn(parentMetadatas);
			} catch (RecordImplException_RecordCannotHaveTwoParents e) {
				validationErrors.add(getClass(), MULTIPLE_PARENTS, new HashMap<String, String>());
			}
		}
	}

}
