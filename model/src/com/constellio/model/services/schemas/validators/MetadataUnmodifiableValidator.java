package com.constellio.model.services.schemas.validators;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.services.records.RecordImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataUnmodifiableValidator implements Validator<Record> {

	public static final String UNMODIFIABLE_METADATA = "modifiedUnmodifiableMetadata";

	private final List<Metadata> metadatas;

	public MetadataUnmodifiableValidator(List<Metadata> metadatas) {
		this.metadatas = metadatas;
	}

	@Override
	public void validate(Record record, ValidationErrors validationErrors, boolean skipNonEssential) {

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
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(METADATA_CODE, metadata.getCode());
		parameters.put(METADATA_LABEL, metadata.getLabelsByLanguageCodes());
		validationErrors.add(getClass(), errorCode, parameters);
	}
}
