package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class MetadataUniqueValidator implements Validator<Record> {
	public static final String NON_UNIQUE_METADATA = "nonUniqueMetadata";
	public static final String METADATA_LABEL = "metadataLabel";
	public static final String VALUE = "value";
	public static final String NON_UNIQUE_CALULATED_METADATA = "nonUniqueCalculatedMetadata";
	public static final String METADATA_DEPENDENCY_LIST = "metadataDependecyList";

	private final List<Metadata> metadatas;
	private final SearchServices searchServices;
	private final MetadataSchemaTypes schemaTypes;

	public MetadataUniqueValidator(List<Metadata> metadatas, MetadataSchemaTypes schemaTypes,
								   SearchServices searchServices) {
		this.metadatas = metadatas;
		this.searchServices = searchServices;
		this.schemaTypes = schemaTypes;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void validate(Record record, ValidationErrors validationErrors, boolean skipNonEssential) {
		if (!record.isActive()) {
			return;
		}
		for (Metadata metadata : metadatas) {
			if (metadata.isUniqueValue() && record.isModified(metadata)) {
				Object value = record.get(metadata);

				MetadataValueType type = metadata.getType();
				boolean isText = type == MetadataValueType.STRING || type == MetadataValueType.TEXT;
				boolean isTextAndEmpty = false;
				if (isText && value != null) {
					isTextAndEmpty = value.toString().isEmpty();
				}

				if (value != null && !isTextAndEmpty) {
					String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(metadata);
					LogicalSearchCondition condition = from(schemaTypes.getSchemaType(schemaTypeCode)).where(metadata)
							.isEqualTo(value).andWhere(Schemas.IDENTIFIER).isNotEqual(record.getId());

					if (searchServices.hasResults(new LogicalSearchQuery(condition).filteredByStatus(StatusFilter.ACTIVES))) {
						if (metadata.getDataEntry().getType() == DataEntryType.CALCULATED) {
							CalculatedDataEntry calculatedDataEntry = (CalculatedDataEntry) metadata.getDataEntry();
							List<? extends Dependency> dependencyList = calculatedDataEntry.getCalculator().getDependencies();

							Map<String, String> dependencyLanguageMap = new HashMap<>();

							for (Dependency dependency : dependencyList) {
								Metadata metadataDependedOn = schemaTypes.getSchemaOf(record).get(dependency.getLocalMetadataCode());
								for (Language language : metadataDependedOn.getLabels().keySet()) {
									String currentValue = dependencyLanguageMap.get(language.getCode());
									if (currentValue == null) {
										currentValue = "";
									} else {
										currentValue += ", ";
									}
									String newValue = currentValue + metadataDependedOn.getLabel(language);
									dependencyLanguageMap.put(language.getCode(), newValue);
								}
							}
							addValidationErrors(validationErrors, dependencyLanguageMap, NON_UNIQUE_CALULATED_METADATA, value.toString());
						} else {
							addValidationErrors(validationErrors, value.toString(), NON_UNIQUE_METADATA, metadata);
						}
					}
				}
			}
		}
	}

	private void addValidationErrors(ValidationErrors validationErrors,
									 Map<String, String> metadatadependedOnMapByLanguage, String errorCode,
									 String value) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(METADATA_DEPENDENCY_LIST, metadatadependedOnMapByLanguage);
		parameters.put(VALUE, value);
		validationErrors.add(getClass(), errorCode, parameters);
	}

	private void addValidationErrors(ValidationErrors validationErrors, String value, String errorCode,
									 Metadata metadata) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(METADATA_CODE, metadata.getCode());
		parameters.put(METADATA_LABEL, metadata.getLabelsByLanguageCodes());
		parameters.put(VALUE, value);
		validationErrors.add(getClass(), errorCode, parameters);
	}
}
