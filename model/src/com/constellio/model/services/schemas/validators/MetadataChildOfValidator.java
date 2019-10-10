package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.services.records.RecordImplRuntimeException.RecordImplException_RecordCannotHaveTwoParents;

import java.util.HashMap;
import java.util.List;

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
		List<Metadata> parentMetadatas = schemaTypes.getSchemaOf(record).getParentReferences();
		if (!parentMetadatas.isEmpty()) {
			try {
				record.getNonNullValueIn(parentMetadatas);
			} catch (RecordImplException_RecordCannotHaveTwoParents e) {
				validationErrors.add(getClass(), MULTIPLE_PARENTS, new HashMap<String, Object>());
			}
		}
	}

}
