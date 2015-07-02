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

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.services.records.RecordImpl;

public class MetadataUnmodifiableValidator implements Validator<Record> {

	public static final String UNMODIFIABLE_METADATA = "modifiedUnmodifiableMetadata";

	private final List<Metadata> metadatas;

	public MetadataUnmodifiableValidator(List<Metadata> metadatas) {
		this.metadatas = metadatas;
	}

	@Override
	public void validate(Record record, ValidationErrors validationErrors) {

		for (Metadata metadata : metadatas) {
			if (metadata.isUnmodifiable()) {
				RecordDTO recordDTO = ((RecordImpl) record).getRecordDTO();
				if (recordDTO != null) {
					Object currentValue = recordDTO.getFields().get(metadata.getDataStoreCode());
					if (currentValue != null && record.isModified(metadata)) {
						addValidationErrors(validationErrors, UNMODIFIABLE_METADATA, metadata);
					}
				}
			}
		}

		//Validate no unmodifiable metadata are modified
		//		if (record.isSaved()) {
		//			RecordImpl recordImpl = (RecordImpl) record;
		//			for (Metadata metadata : metadatas) {
		//				if (metadata.isUnmodifiable()) {
		//					boolean wasInDTO = recordImpl.getRecordDTO().getFields().containsKey(metadata.getDataStoreCode());
		//					boolean isModified = recordImpl.isModified(metadata);
		//
		//					if (wasInDTO && isModified) {
		//						addValidationErrors(validationErrors, UNMODIFIABLE_METADATA, metadata);
		//					}
		//				}
		//			}
		//		}
	}

	private void addValidationErrors(ValidationErrors validationErrors, String errorCode, Metadata metadata) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(METADATA_CODE, metadata.getCode());
		parameters.put(METADATA_LABEL, metadata.getLabel());
		validationErrors.add(getClass(), errorCode, parameters);
	}
}
