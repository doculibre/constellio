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
import java.util.Map;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.services.records.RecordProvider;

public class CyclicHierarchyValidator implements Validator<Record> {

	public static final String CANNOT_REFERENCE_A_DESCENDANT_IN_A_CHILD_OF_REFERENCE = "cannotReferenceADescendantInAChildOfReference";
	public static final String UNALLOWED_CODE = "unallowedCode";

	private final List<Metadata> metadatas;
	private final MetadataSchemaTypes schemaTypes;
	private final RecordProvider recordProvider;

	public CyclicHierarchyValidator(MetadataSchemaTypes schemaTypes, List<Metadata> metadatas,
			RecordProvider recordProvider) {
		this.schemaTypes = schemaTypes;
		this.metadatas = metadatas;
		this.recordProvider = recordProvider;

	}

	@Override
	public void validate(Record record, ValidationErrors validationErrors) {
		for (Metadata metadata : metadatas) {
			if (metadata.getType() == MetadataValueType.REFERENCE && record.isModified(metadata)
					&& record.get(metadata) != null) {
				if (!metadata.isMultivalue()) {
					String referenceValue = record.get(metadata);
					Record referencedRecord = recordProvider.getRecord(referenceValue);
					MetadataSchema schema = getSchema(referencedRecord);
					if (metadata.isChildOfRelationship()) {
						String principalPath = (String) referencedRecord.get(Schemas.PRINCIPAL_PATH);
						if (principalPath != null && principalPath.contains(record.getId())) {
							addValidationErrors(validationErrors, CANNOT_REFERENCE_A_DESCENDANT_IN_A_CHILD_OF_REFERENCE,
									metadata, schema.getCode());
						}

					}
				}

			}
		}
	}

	private MetadataSchema getSchema(Record referencedRecord) {

		String referencedSchemaCode = referencedRecord.getSchemaCode();
		return schemaTypes.getSchema(referencedSchemaCode);
	}

	public void addValidationErrors(ValidationErrors validationErrors, String code, Metadata metadata, String unallowedSchema) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(METADATA_CODE, metadata.getCode());
		parameters.put(METADATA_LABEL, metadata.getLabel());
		parameters.put(UNALLOWED_CODE, unallowedSchema);
		validationErrors.add(getClass(), code, parameters);
	}
}
