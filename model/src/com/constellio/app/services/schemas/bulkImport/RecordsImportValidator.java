package com.constellio.app.services.schemas.bulkImport;

import static com.constellio.app.services.schemas.bulkImport.RecordsImportServices.ALL_BOOLEAN_NO;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportServices.ALL_BOOLEAN_YES;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.CannotGetMetadatasOfAnotherSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.events.recordsImport.PrevalidationParams;
import com.constellio.model.frameworks.validation.DecoratedValidationsErrors;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.ContentImport;
import com.constellio.model.services.records.bulkImport.ProgressionHandler;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class RecordsImportValidator {

	public static final String LEGACY_ID_LOCAL_CODE = Schemas.LEGACY_ID.getLocalCode();
	public static final String DISABLED_METADATA_CODE = "disabledMetadataCode";
	public static final String SYSTEM_RESERVED_METADATA_CODE = "systemReservedMetadataCode";
	public static final String AUTOMATIC_METADATA_CODE = "automaticMetadataCode";

	public static final String INVALID_RESOLVER_METADATA_CODE = "invalidResolverMetadataCode";
	public static final String INVALID_METADATA_CODE = "invalidMetadataCode";
	public static final String INVALID_SCHEMA_CODE = "invalidSchemaCode";
	public static final String LEGACY_ID_NOT_UNIQUE = "legacyIdNotUnique";
	public static final String METADATA_NOT_UNIQUE = "metadataNotUnique";

	public static final String REQUIRED_VALUE = "requiredValue";
	public static final String INVALID_SINGLEVALUE = "invalidSinglevalue";
	public static final String INVALID_MULTIVALUE = "invalidMultivalue";
	public static final String INVALID_STRING_VALUE = "invalidStringValue";
	public static final String INVALID_NUMBER_VALUE = "invalidNumberValue";
	public static final String INVALID_CONTENT_VALUE = "invalidContentValue";
	public static final String INVALID_STRUCTURE_VALUE = "invalidStructureValue";
	public static final String INVALID_BOOLEAN_VALUE = "invalidBooleanValue";
	public static final String INVALID_DATE_VALUE = "invalidDateValue";
	public static final String INVALID_DATETIME_VALUE = "invalidDatetimeValue";
	public static final String INVALID_ENUM_VALUE = "invalidEnumValue";
	public static final String UNRESOLVED_VALUE = "unresolvedValue";
	public static final String REQUIRED_ID = "requiredId";

	String schemaType;
	ImportDataProvider importDataProvider;
	MetadataSchemaTypes types;
	MetadataSchemaType type;
	ResolverCache resolverCache;
	ModelLayerCollectionExtensions extensions;
	ProgressionHandler progressionHandler;
	Language language;

	public RecordsImportValidator(String schemaType, ProgressionHandler progressionHandler,
			ImportDataProvider importDataProvider, MetadataSchemaTypes types,
			ResolverCache resolverCache, ModelLayerCollectionExtensions extensions, Language language) {
		this.schemaType = schemaType;
		this.importDataProvider = importDataProvider;
		this.extensions = extensions;
		this.types = types;
		this.type = types.getSchemaType(schemaType);
		this.resolverCache = resolverCache;
		this.progressionHandler = progressionHandler;
		this.language = language;
	}

	public void validate()
			throws ValidationException {

		Iterator<ImportData> importDataIterator = importDataProvider.newDataIterator(schemaType);

		ValidationErrors errors = new DecoratedValidationsErrors(new ValidationErrors()) {
			@Override
			public void buildExtraParams(Map<String, Object> parameters) {
				if (!parameters.containsKey("schemaType")) {
					parameters.put("schemaType", schemaType);
				}
			}
		};

		validate(importDataIterator, errors);

		errors.throwIfNonEmpty();
	}

	private void validate(Iterator<ImportData> importDataIterator, ValidationErrors errors) {
		progressionHandler.beforeValidationOfSchema(schemaType);
		int numberOfRecords = 0;
		List<String> uniqueMetadatas = type.getAllMetadatas().onlyWithType(STRING).onlyUniques().toLocalCodesList();
		while (importDataIterator.hasNext()) {

			final ImportData importData = importDataIterator.next();
			numberOfRecords++;
			if (importData.getLegacyId() == null) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put("prefix", type.getLabel(language) + " : ");
				parameters.put("index", "" + (importData.getIndex() + 1));

				errors.add(RecordsImportServices.class, REQUIRED_ID, parameters);
			} else {
				DecoratedValidationsErrors decoratedErrors = new DecoratedValidationsErrors(errors) {
					@Override
					public void buildExtraParams(Map<String, Object> parameters) {
						String schemaTypeLabel = type.getLabel(language);
						if (importData.getValue("code") != null) {
							parameters.put("prefix", schemaTypeLabel + " " + importData.getValue("code") + " : ");
						} else {
							parameters.put("prefix", schemaTypeLabel + " " + importData.getLegacyId() + " : ");
						}

						parameters.put("index", "" + (importData.getIndex() + 1));
						parameters.put("legacyId", importData.getLegacyId());
					}
				};
				try {
					validateValueUnicityOfUniqueMetadata(uniqueMetadatas, importData, decoratedErrors);

					markUniqueValuesAsInFile(uniqueMetadatas, importData);
					MetadataSchema metadataSchema = type.getSchema(importData.getSchema());

					validateFields(importData, metadataSchema, decoratedErrors);

					boolean isUpdate = resolverCache.isRecordUpdate(schemaType, importData.getLegacyId());

					if (!isUpdate) {
						validateMetadatasRequirement(importData, metadataSchema, decoratedErrors);
					}
				} catch (MetadataSchemasRuntimeException.NoSuchSchema | CannotGetMetadatasOfAnotherSchemaType e) {
					decoratedErrors
							.add(RecordsImportServices.class, INVALID_SCHEMA_CODE, asMap("schema", importData.getSchema()));
				}

				String schemaTypeLabel = types.getSchemaType(schemaType).getLabel(language);
				extensions.callRecordImportPrevalidate(schemaType, new PrevalidationParams(decoratedErrors, importData));
			}

		}

		validateAllReferencesResolved(errors);
		progressionHandler.afterValidationOfSchema(schemaType, numberOfRecords);
	}

	private void validateAllReferencesResolved(ValidationErrors errors) {
		for (MetadataSchemaType schemaType : resolverCache.getCachedSchemaTypes())
			for (Metadata uniqueValueMetadata : schemaType.getAllMetadatas().onlyUniques()) {
				List<String> unresolved = new ArrayList<>(
						resolverCache.getUnresolvableUniqueValues(schemaType.getCode(), uniqueValueMetadata.getLocalCode()));
				Collections.sort(unresolved);
				if (!unresolved.isEmpty()) {
					for (String value : unresolved) {
						Map<String, Object> parameters = new HashMap<>();
						parameters.put("metadata", uniqueValueMetadata.getLocalCode());
						parameters.put("metadataLabel", uniqueValueMetadata.getLabel(language));
						parameters.put("referencedSchemaType", schemaType.getCode());
						parameters.put("referencedSchemaTypeLabel", schemaType.getLabel(language));
						parameters.put("unresolvedValue", value);
						parameters.put("prefix", schemaType.getLabel(language) + " : ");
						errors.add(RecordsImportServices.class, UNRESOLVED_VALUE, parameters);
					}
				}
			}
	}

	private Map<String, Object> asMap(String key, Object value) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		return map;
	}

	private void validateValueUnicityOfUniqueMetadata(List<String> uniqueMetadatas, ImportData importData,
			ValidationErrors errors) {
		if (!resolverCache.isNewUniqueValue(type.getCode(), LEGACY_ID_LOCAL_CODE, importData.getLegacyId())) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("value", importData.getLegacyId());
			errors.add(RecordsImportServices.class, LEGACY_ID_NOT_UNIQUE, parameters);
		} else {

			for (String uniqueMetadata : uniqueMetadatas) {
				String uniqueValue = (String) importData.getFields().get(uniqueMetadata);

				if (uniqueValue != null && !resolverCache.isNewUniqueValue(type.getCode(), uniqueMetadata, uniqueValue)) {
					Metadata metadata = type.getSchema(importData.getSchema()).getMetadata(uniqueMetadata);
					Map<String, Object> parameters = toMetadataParameters(metadata);
					parameters.put("value", uniqueValue);
					errors.add(RecordsImportServices.class, METADATA_NOT_UNIQUE, parameters);
				}

			}
		}
	}

	private void markUniqueValuesAsInFile(List<String> uniqueMetadatas, ImportData importData) {
		resolverCache.markAsRecordInFile(type.getCode(), LEGACY_ID_LOCAL_CODE, importData.getLegacyId());
		for (String uniqueMetadata : uniqueMetadatas) {
			String value = (String) importData.getFields().get(uniqueMetadata);
			if (value != null) {
				resolverCache.markAsRecordInFile(type.getCode(), uniqueMetadata, value);
			}

		}
	}

	private void validateMetadatasRequirement(ImportData importData, MetadataSchema metadataSchema,
			ValidationErrors errors) {
		for (Metadata requiredMetadata : metadataSchema.getMetadatas().onlyAlwaysRequired().onlyNonSystemReserved()
				.onlyManuals()) {

			Object fieldValue = importData.getFields().get(requiredMetadata.getLocalCode());

			if (fieldValue == null || (fieldValue instanceof List && ((List) fieldValue).isEmpty())) {
				errors.add(RecordsImportServices.class, REQUIRED_VALUE, toMetadataParameters(requiredMetadata));
			}
		}

	}

	private void validateFields(ImportData importData, MetadataSchema metadataSchema, ValidationErrors errors) {
		for (Entry<String, Object> entry : importData.getFields().entrySet()) {
			if (entry.getValue() != null) {
				try {
					final Metadata metadata = metadataSchema.getMetadata(entry.getKey());
					validateMetadata(metadata, errors);
					DecoratedValidationsErrors decoratedErrors = new DecoratedValidationsErrors(errors) {
						@Override
						public void buildExtraParams(Map<String, Object> params) {
							params.put("metadata", metadata.getLocalCode());
							params.put("metadataLabel", metadata.getLabel(language));
						}
					};
					validateValue(importData.getIndex(), importData.getLegacyId(), metadata, entry.getValue(), decoratedErrors);
					if (!decoratedErrors.hasDecoratedErrors()) {
						if (metadata.getType() == REFERENCE && metadata.isMultivalue()) {
							for (String resolver : (List<String>) entry.getValue()) {
								feedLegacyIdResolver(importData, metadata, resolver, decoratedErrors);
							}

						} else if (metadata.getType() == REFERENCE && !metadata.isMultivalue()) {
							feedLegacyIdResolver(importData, metadata, (String) entry.getValue(), decoratedErrors);
						}
					}

				} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("metadata", entry.getKey());
					parameters.put("schema", metadataSchema.getCode());
					parameters.put("schemaLabel", metadataSchema.getLabel(language));

					errors.add(RecordsImportServices.class, INVALID_METADATA_CODE, parameters);
				}
			}
		}
	}

	private void validateMetadata(Metadata metadata, ValidationErrors errors) {
		if (metadata.isSystemReserved()) {
			//return SYSTEM_RESERVED_METADATA_CODE;
		} else if (!metadata.isEnabled()) {
			//return DISABLED_METADATA_CODE;
		} else if (metadata.getDataEntry().getType() != DataEntryType.MANUAL) {

			errors.add(RecordsImportServices.class, AUTOMATIC_METADATA_CODE, toMetadataParameters(metadata));
		}
	}

	private void feedLegacyIdResolver(ImportData importData, Metadata metadata, String resolverStr, ValidationErrors errors) {
		String schemaType = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
		Resolver resolver = Resolver.toResolver(resolverStr);
		MetadataSchemaType type = types.getSchemaType(schemaType);

		if (type.getAllMetadatas().getMetadataWithLocalCode(resolver.metadata) == null) {
			errors.add(RecordsImportServices.class, INVALID_RESOLVER_METADATA_CODE, asMap("resolverMetadata", resolver.metadata));
		}

		resolverCache.markUniqueValueAsRequired(schemaType, resolver.metadata, resolver.value);
	}

	private void validateValueType(Metadata metadata, final Object value, ValidationErrors errors) {

		MetadataValueType type = metadata.getType();

		if (type == MetadataValueType.DATE) {
			if (!(value instanceof LocalDate)) {
				errors.add(RecordsImportServices.class, INVALID_DATE_VALUE);
			}

		} else if (type == MetadataValueType.DATE_TIME) {
			if (!(value instanceof LocalDateTime)) {
				errors.add(RecordsImportServices.class, INVALID_DATETIME_VALUE);
			}

		} else if (type == MetadataValueType.BOOLEAN) {
			if (!(value instanceof String)) {
				errors.add(RecordsImportServices.class, INVALID_BOOLEAN_VALUE);
			} else {
				String lowerCaseValue = ((String) value).toLowerCase();
				if (!ALL_BOOLEAN_YES.contains(lowerCaseValue) && !ALL_BOOLEAN_NO.contains(lowerCaseValue)) {
					errors.add(RecordsImportServices.class, INVALID_BOOLEAN_VALUE);
				}

			}

		} else if (type == MetadataValueType.ENUM) {

			if (!(value instanceof String)) {
				errors.add(RecordsImportServices.class, INVALID_ENUM_VALUE, toEnumAvailableChoicesParam(metadata));
			} else {
				try {
					EnumWithSmallCodeUtils.toEnum(metadata.getEnumClass(), (String) value);
				} catch (Exception e) {
					errors.add(RecordsImportServices.class, INVALID_ENUM_VALUE, toEnumAvailableChoicesParam(metadata));
				}

			}

		} else if (type == MetadataValueType.NUMBER) {

			try {
				Double.valueOf((String) value);
			} catch (Exception e) {
				errors.add(RecordsImportServices.class, INVALID_NUMBER_VALUE);
			}

		} else if (type == MetadataValueType.CONTENT) {

			if (!ContentImport.class.equals(value.getClass())) {
				errors.add(RecordsImportServices.class, INVALID_CONTENT_VALUE);
			}

		} else if (type == MetadataValueType.STRUCTURE) {

			if (!Map.class.isAssignableFrom(value.getClass())) {
				errors.add(RecordsImportServices.class, RecordsImportValidator.INVALID_STRUCTURE_VALUE);
			}

		} else {
			if (!(value instanceof String)) {
				errors.add(RecordsImportServices.class, RecordsImportValidator.INVALID_STRING_VALUE);
			}
		}
	}

	private Map<String, Object> toEnumAvailableChoicesParam(Metadata metadata) {
		Map<String, Object> parameters = new HashMap<>();
		List<String> choices = EnumWithSmallCodeUtils.toSmallCodeList(metadata.getEnumClass());
		parameters.put("acceptedValues", StringUtils.join(choices, ", "));
		return parameters;
	}

	private void validateValue(final int index, final String legacyId, final Metadata metadata, final Object value,
			ValidationErrors errors) {

		DecoratedValidationsErrors decoratedErrors = new DecoratedValidationsErrors(errors) {
			@Override
			public void buildExtraParams(Map<String, Object> params) {
				params.put("value", value == null ? "null" : value.toString());
			}
		};

		if (value != null) {
			if (metadata.isMultivalue()) {
				if (!(value instanceof List)) {
					Map<String, Object> parameters = new HashMap<>();
					decoratedErrors.add(RecordsImportServices.class, INVALID_MULTIVALUE);
				} else {
					List list = (List) value;

					for (final Object item : list) {
						DecoratedValidationsErrors decoratedErrorsForItem = new DecoratedValidationsErrors(errors) {
							@Override
							public void buildExtraParams(Map<String, Object> params) {
								params.put("value", item == null ? "null" : item.toString());
							}
						};
						validateValueType(metadata, item, decoratedErrorsForItem);
					}
				}
			} else {
				if (value instanceof List) {
					decoratedErrors.add(RecordsImportServices.class, INVALID_SINGLEVALUE);
				} else {
					validateValueType(metadata, value, decoratedErrors);
				}
			}

		}

	}

	private Map<String, Object> toMetadataParameters(Metadata metadata) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("metadata", metadata.getLocalCode());
		parameters.put("metadataLabel", metadata.getLabel(language));
		return parameters;
	}

}
