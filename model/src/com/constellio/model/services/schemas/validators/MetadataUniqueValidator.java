package com.constellio.model.services.schemas.validators;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class MetadataUniqueValidator implements Validator<Record> {
	public static final String NON_UNIQUE_METADATA = "nonUniqueMetadata";
	public static final String METADATA_LABEL = "metadataLabel";
	public static final String VALUE = "value";

	private final List<Metadata> metadatas;
	private final SearchServices searchServices;
	private final MetadataSchemaTypes schemaTypes;

	public MetadataUniqueValidator(List<Metadata> metadatas, MetadataSchemaTypes schemaTypes, SearchServices searchServices) {
		this.metadatas = metadatas;
		this.searchServices = searchServices;
		this.schemaTypes = schemaTypes;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void validate(Record record, ValidationErrors validationErrors) {
		if (!record.isActive()) {
			return;
		}
		for (Metadata metadata : metadatas) {
			if (metadata.isUniqueValue() && record.isModified(metadata)) {
				Object value = record.get(metadata);
				if (value != null) {
					String schemaCode = new SchemaUtils().getSchemaCode(metadata.getCode());
					LogicalSearchCondition condition = from(schemaTypes.getSchema(schemaCode)).where(metadata)
							.isEqualTo(value).andWhere(Schemas.IDENTIFIER).isNotEqual(record.getId());

					if (searchServices.hasResults(new LogicalSearchQuery(condition).filteredByStatus(StatusFilter.ACTIVES))) {
						addValidationErrors(validationErrors, value.toString(), NON_UNIQUE_METADATA, metadata);
					}
				}
			}
		}
	}

	private void addValidationErrors(ValidationErrors validationErrors, String value, String errorCode, Metadata metadata) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(METADATA_CODE, metadata.getCode());
		parameters.put(METADATA_LABEL,metadata.getLabelsByLanguageCodes());
		parameters.put(VALUE, value);
		validationErrors.add(getClass(), errorCode, parameters);
	}
}
