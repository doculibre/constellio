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
package com.constellio.app.services.schemas.bulkImport;

import static com.constellio.data.utils.LangUtils.asMap;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
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
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
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
	public static final String VALUE_NOT_UNIQUE = "valueNotUnique";

	public static final String REQUIRED_VALUE = "requiredValue";
	public static final String INVALID_SINGLEVALUE = "invalidSinglevalue";
	public static final String INVALID_MULTIVALUE = "invalidMultivalue";
	public static final String INVALID_STRING_VALUE = "invalidStringValue";
	public static final String INVALID_NUMBER_VALUE = "invalidNumberValue";
	public static final String INVALID_CONTENT_VALUE = "invalidContentValue";
	public static final String INVALID_BOOLEAN_VALUE = "invalidBooleanValue";
	public static final String INVALID_DATE_VALUE = "invalidDateValue";
	public static final String INVALID_DATETIME_VALUE = "invalidDatetimeValue";
	public static final String INVALID_ENUM_VALUE = "invalidEnumValue";
	public static final String UNRESOLVED_VALUE = "unresolvedValue";
	public static final String REQUIRED_IDS = "requiredIds";

	String schemaType;
	ImportDataProvider importDataProvider;
	MetadataSchemaTypes types;
	MetadataSchemaType type;
	ResolverCache resolverCache;
	ModelLayerCollectionExtensions extensions;
	ProgressionHandler progressionHandler;
	ValidationErrors errors = new ValidationErrors();

	public RecordsImportValidator(String schemaType, ProgressionHandler progressionHandler,
			ImportDataProvider importDataProvider, MetadataSchemaTypes types,
			ResolverCache resolverCache, ModelLayerCollectionExtensions extensions) {
		this.schemaType = schemaType;
		this.importDataProvider = importDataProvider;
		this.extensions = extensions;
		this.types = types;
		this.type = types.getSchemaType(schemaType);
		this.resolverCache = resolverCache;
		this.progressionHandler = progressionHandler;
	}

	public void validate() {

		Iterator<ImportData> importDataIterator = importDataProvider.newDataIterator(schemaType);

		validate(importDataIterator);

		if (!errors.getValidationErrors().isEmpty()) {
			throw new ValidationRuntimeException(errors);
		}
	}

	private void validate(Iterator<ImportData> importDataIterator) {
		progressionHandler.beforeValidationOfSchema(schemaType);
		int numberOfRecords = 0;
		List<String> uniqueMetadatas = type.getAllMetadatas().onlyWithType(STRING).onlyUniques().toLocalCodesList();
		while (importDataIterator.hasNext()) {
			ImportData importData = null;
			try {
				importData = importDataIterator.next();
				numberOfRecords++;
				if (importData.getLegacyId() == null) {
					error(REQUIRED_IDS, asMap("index", "" + (importData.getIndex() + 1)));
				} else {

					validateValueUnicityOfUniqueMetadata(uniqueMetadatas, importData);

					markUniqueValuesAsInFile(uniqueMetadatas, importData);
					MetadataSchema metadataSchema = type.getSchema(importData.getSchema());

					validateFields(importData, metadataSchema);

					boolean isUpdate = resolverCache.isRecordUpdate(schemaType, importData.getLegacyId());

					if (!isUpdate) {
						validateMetadatasRequirement(importData, metadataSchema);
					}
				}

			} catch (MetadataSchemasRuntimeException.NoSuchSchema | CannotGetMetadatasOfAnotherSchemaType e) {
				error(INVALID_SCHEMA_CODE, importData, asMap("schema", importData.getSchema()));
			}

			ImportDataErrors importDataErrors = new ImportDataErrors(schemaType, errors, importData);
			extensions.callRecordImportPrevalidate(schemaType, new PrevalidationParams(importDataErrors, importData));

		}

		validateAllReferencesResolved();
		progressionHandler.afterValidationOfSchema(schemaType, numberOfRecords);
	}

	private void validateAllReferencesResolved() {
		for (MetadataSchemaType schemaType : resolverCache.getCachedSchemaTypes())
			for (String uniqueValueMetadata : schemaType.getAllMetadatas().onlyUniques().toLocalCodesList()) {
				List<String> unresolved = new ArrayList<>(
						resolverCache.getUnresolvableUniqueValues(schemaType.getCode(), uniqueValueMetadata));
				Collections.sort(unresolved);
				if (!unresolved.isEmpty()) {
					Map<String, String> parameters = asMap(uniqueValueMetadata, unresolved.toString(), "schemaType",
							schemaType.getCode());
					error(UNRESOLVED_VALUE, parameters);
				}
			}
	}

	private void validateValueUnicityOfUniqueMetadata(List<String> uniqueMetadatas, ImportData importData) {
		if (!resolverCache.isNewUniqueValue(type.getCode(), LEGACY_ID_LOCAL_CODE, importData.getLegacyId())) {
			error(LEGACY_ID_NOT_UNIQUE, asMap("legacyId", importData.getLegacyId()));
		}

		//		for (String uniqueMetadata : uniqueMetadatas) {
		//			String uniqueValue = (String) importData.getFields().get(uniqueMetadata);
		//
		//			if (!resolverCache.isNewUniqueValue(type.getCode(), uniqueMetadata, uniqueValue)) {
		//				error(VALUE_NOT_UNIQUE, asMap("value", uniqueValue));
		//			}
		//		}
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

	private void validateMetadatasRequirement(ImportData importData, MetadataSchema metadataSchema) {
		List<String> missingRequiredMetadatas = new ArrayList<>();
		for (Metadata requiredMetadata : metadataSchema.getMetadatas().onlyAlwaysRequired().onlyNonSystemReserved()
				.onlyManuals()) {

			Object fieldValue = importData.getFields().get(requiredMetadata.getLocalCode());

			if (fieldValue == null || (fieldValue instanceof List && ((List) fieldValue).isEmpty())) {
				missingRequiredMetadatas.add(requiredMetadata.getLocalCode());
			}
		}

		if (!missingRequiredMetadatas.isEmpty()) {
			error(REQUIRED_VALUE, importData, asMap("metadatas", missingRequiredMetadatas.toString()));
		}
	}

	private void validateFields(ImportData importData, MetadataSchema metadataSchema) {
		for (Entry<String, Object> entry : importData.getFields().entrySet()) {
			if (entry.getValue() != null) {
				try {
					Metadata metadata = metadataSchema.getMetadata(entry.getKey());
					String errorCode = validateMetadata(metadata);
					if (errorCode != null) {
						error(errorCode, importData, asMap("metadata", entry.getKey()));
					}
					if (validateValue(importData.getIndex(), importData.getLegacyId(), metadata, entry.getValue())) {
						if (metadata.getType() == REFERENCE && metadata.isMultivalue()) {
							for (String resolver : (List<String>) entry.getValue()) {
								feedLegacyIdResolver(importData, metadata, resolver);
							}

						} else if (metadata.getType() == REFERENCE && !metadata.isMultivalue()) {
							feedLegacyIdResolver(importData, metadata, (String) entry.getValue());
						}
					}

				} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
					error(INVALID_METADATA_CODE, importData, asMap("metadata", entry.getKey()));
				}
			}
		}
	}

	private String validateMetadata(Metadata metadata) {
		if (metadata.isSystemReserved()) {
			//return SYSTEM_RESERVED_METADATA_CODE;
		} else if (!metadata.isEnabled()) {
			//return DISABLED_METADATA_CODE;
		} else if (metadata.getDataEntry().getType() != DataEntryType.MANUAL) {
			return AUTOMATIC_METADATA_CODE;
		}
		return null;
	}

	private void feedLegacyIdResolver(ImportData importData, Metadata metadata, String resolverStr) {
		String schemaType = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
		Resolver resolver = Resolver.toResolver(resolverStr);
		MetadataSchemaType type = types.getSchemaType(schemaType);

		if (type.getAllMetadatas().getMetadataWithLocalCode(resolver.metadata) == null) {
			error(INVALID_RESOLVER_METADATA_CODE, importData, asMap("metadata", resolver.metadata));
		}

		resolverCache.markUniqueValueAsRequired(schemaType, resolver.metadata, resolver.value);
	}

	private String validateValueType(Metadata metadata, Object value, Map<String, String> parameters) {
		MetadataValueType type = metadata.getType();

		if (type == MetadataValueType.DATE) {
			if (!(value instanceof LocalDate)) {
				return INVALID_DATE_VALUE;
			}

		} else if (type == MetadataValueType.DATE_TIME) {
			if (!(value instanceof LocalDateTime)) {
				return INVALID_DATETIME_VALUE;
			}

		} else if (type == MetadataValueType.BOOLEAN) {
			if (!(value instanceof String)) {
				return INVALID_BOOLEAN_VALUE;
			} else {
				String lowerCaseValue = ((String) value).toLowerCase();
				if (!RecordsImportServices.ALL_BOOLEAN_YES.contains(lowerCaseValue) && !RecordsImportServices.ALL_BOOLEAN_NO
						.contains(lowerCaseValue)) {
					return INVALID_BOOLEAN_VALUE;
				}

			}

		} else if (type == MetadataValueType.ENUM) {
			if (!(value instanceof String)) {
				return INVALID_ENUM_VALUE;
			} else {
				try {
					EnumWithSmallCodeUtils.toEnum(metadata.getEnumClass(), (String) value);
				} catch (Exception e) {
					List<String> choices = EnumWithSmallCodeUtils.toSmallCodeList(metadata.getEnumClass());
					parameters.put("availableChoices", choices.toString());
					return INVALID_ENUM_VALUE;
				}

			}

		} else if (type == MetadataValueType.NUMBER) {

			try {
				Double.valueOf((String) value);
			} catch (Exception e) {
				return INVALID_NUMBER_VALUE;
			}

		} else if (type == MetadataValueType.CONTENT) {

			if (!ContentImport.class.equals(value.getClass())) {
				return INVALID_CONTENT_VALUE;
			}

		} else if (type == MetadataValueType.STRUCTURE) {

			if (!Map.class.isAssignableFrom(value.getClass())) {
				return INVALID_CONTENT_VALUE;
			}

		} else {
			if (!(value instanceof String)) {
				return INVALID_STRING_VALUE;
			}
		}
		return null;
	}

	private boolean validateValue(int index, String legacyId, Metadata metadata, Object value) {

		String errorCode = null;
		Map<String, String> parameters = new HashMap<>();
		if (value != null) {
			if (metadata.isMultivalue()) {
				if (!(value instanceof List)) {
					errorCode = INVALID_MULTIVALUE;
				} else {
					List list = (List) value;

					for (Object item : list) {
						if (errorCode == null) {
							errorCode = validateValueType(metadata, item, parameters);
						}
					}
				}
			} else {
				if (value instanceof List) {
					errorCode = INVALID_SINGLEVALUE;
				} else {
					errorCode = validateValueType(metadata, value, parameters);
				}
			}

		}

		if (errorCode != null) {

			parameters.put("index", "" + (index + 1));
			parameters.put("legacyId", legacyId);
			parameters.put("metadata", metadata.getLocalCode());
			parameters.put("schemaType", schemaType);
			parameters.put("invalidValue", value.toString());
			error(errorCode, parameters);
			return false;
		}

		return true;
	}

	private void error(String code, Map<String, String> parameters) {
		if (!parameters.containsKey("schemaType")) {
			parameters.put("schemaType", schemaType);
		}
		errors.add(RecordsImportServices.class, code, parameters);
	}

	private void error(String code, ImportData importData, Map<String, String> parameters) {
		parameters.put("index", "" + (importData.getIndex() + 1));
		parameters.put("legacyId", importData.getLegacyId());
		parameters.put("schemaType", schemaType);
		errors.add(RecordsImportServices.class, code, parameters);
	}

}
