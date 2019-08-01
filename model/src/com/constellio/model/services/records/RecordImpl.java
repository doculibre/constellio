package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordRuntimeException;
import com.constellio.model.entities.records.RecordRuntimeException.InvalidMetadata;
import com.constellio.model.entities.records.RecordRuntimeException.RecordIsAlreadySaved;
import com.constellio.model.entities.records.RecordRuntimeException.RecordRuntimeException_CannotModifyId;
import com.constellio.model.entities.records.RecordRuntimeException.RequiredMetadataArgument;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchMetadata;
import com.constellio.model.entities.schemas.MetadataTransiency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.records.RecordImplRuntimeException.CannotGetListForSingleValue;
import com.constellio.model.services.records.RecordImplRuntimeException.RecordImplException_CannotBuildStructureValue;
import com.constellio.model.services.records.RecordImplRuntimeException.RecordImplException_PopulatorReturnedNullValue;
import com.constellio.model.services.records.RecordImplRuntimeException.RecordImplException_RecordCannotHaveTwoParents;
import com.constellio.model.services.records.RecordImplRuntimeException.RecordImplException_UnsupportedOperationOnUnsavedRecord;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import static com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval.PREFERRING;
import static com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval.STRICT;
import static com.constellio.model.entities.schemas.entries.DataEntryType.CALCULATED;
import static com.constellio.model.entities.schemas.entries.DataEntryType.MANUAL;
import static com.constellio.model.entities.schemas.entries.DataEntryType.SEQUENCE;
import static com.constellio.model.services.records.RecordUtils.estimateRecordUpdateSize;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class RecordImpl implements Record {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordImpl.class);

	protected Map<String, Object> modifiedValues = new HashMap<String, Object>();
	private Record unmodifiableCopyOfOriginalRecord;
	private String schemaCode;
	private String schemaTypeCode;
	private final String collection;
	private final String id;
	private long version;
	private boolean disconnected = false;
	private RecordDTO recordDTO;
	private Map<String, Object> lazyTransientValues = new HashMap<String, Object>();
	private Map<String, Object> eagerTransientValues = new HashMap<String, Object>();
	private Map<String, Object> structuredValues;
	private boolean unmodifiable;
	private CollectionInfo collectionInfo;

	private RecordDTO lastCreatedRecordDTO;
	private RecordDeltaDTO lastCreatedDeltaDTO;

	public RecordImpl(MetadataSchema schema, String id) {
		if (schema == null) {
			throw new IllegalArgumentException("Require schema");
		}
		this.collection = schema.getCollection();

		this.id = id;
		this.schemaCode = schema.getCode();
		this.schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);
		this.version = -1;
		this.recordDTO = null;
		this.collectionInfo = schema.getCollectionInfo();
	}

	private RecordImpl(RecordDTO recordDTO, CollectionInfo collectionInfo, Map<String, Object> eagerTransientValues) {
		this(recordDTO, collectionInfo);
		this.eagerTransientValues = new HashMap<>(eagerTransientValues);
	}

	private RecordImpl(RecordDTO recordDTO, CollectionInfo collectionInfo, Map<String, Object> eagerTransientValues,
					   boolean unmodifiable) {
		this(recordDTO, collectionInfo);
		this.eagerTransientValues = new HashMap<>(eagerTransientValues);
		this.unmodifiable = unmodifiable;
		if (unmodifiable) {
			this.modifiedValues = Collections.unmodifiableMap(modifiedValues);
		}
	}

	public RecordImpl(MetadataSchema schema, RecordDTO recordDTO) {
		this(recordDTO, schema.getCollectionInfo());
	}

	public RecordImpl(RecordDTO recordDTO, CollectionInfo collectionInfo) {
		this.id = recordDTO.getId();
		this.version = recordDTO.getVersion();
		this.schemaCode = (String) recordDTO.getFields().get("schema_s");
		this.collection = (String) recordDTO.getFields().get("collection_s");
		if (collection == null) {
			throw new IllegalArgumentException("Require collection code for record '" + id + "'");
		}

		this.recordDTO = recordDTO;
		this.schemaTypeCode = schemaCode == null ? null : SchemaUtils.getSchemaTypeCode(schemaCode);
		this.collectionInfo = collectionInfo;
	}

	public boolean isSummary() {
		return recordDTO.getLoadingMode() == RecordDTOMode.SUMMARY;
	}

	@Override
	public RecordDTOMode getLoadedFieldsMode() {
		return recordDTO.getLoadingMode();
	}

	public Record updateAutomaticValue(Metadata metadata, Object value) {
		return updateAutomaticValue(metadata, value, collectionInfo.getMainSystemLocale());
	}

	public Record updateAutomaticValue(Metadata metadata, Object value, Locale locale) {

		get(metadata);
		Object convertedRecord;
		if (metadata.getEnumClass() != null) {
			if (metadata.isMultivalue()) {
				convertedRecord = EnumWithSmallCodeUtils.toSmallCodeList((List<Enum<?>>) value);
			} else {
				convertedRecord = EnumWithSmallCodeUtils.toSmallCode((Enum<?>) value);
			}
		} else {
			convertedRecord = value;
		}

		if (value instanceof List) {
			return setModifiedValue(metadata, locale.getLanguage(), unmodifiableList((List<?>) convertedRecord));
		} else {
			return setModifiedValue(metadata, locale.getLanguage(), convertedRecord);
		}

	}

	@Override
	public Record set(Metadata metadata, Object value) {
		return set(metadata, collectionInfo.getMainSystemLanguage().getCode(), value);
	}

	@Override
	public Record set(Metadata metadata, Locale locale, Object value) {
		return set(metadata, locale == null ? collectionInfo.getMainSystemLanguage().getCode() : locale.getLanguage(), value);
	}

	private Record set(Metadata metadata, String language, Object value) {
		ensureModifiable();
		if ("".equals(value)) {
			value = null;
		}

		// Get may parse some metadata, and this is required later
		get(metadata);
		validateMetadata(metadata);
		if (!metadata.isMultivalue()) {
			validateScalarValue(metadata, value);
		}

		Object convertedRecord;
		if (value instanceof Record) {
			convertedRecord = ((Record) value).getId();

		} else if (value instanceof RecordWrapper) {
			convertedRecord = ((RecordWrapper) value).getWrappedRecord().getId();

		} else if (metadata.getEnumClass() != null) {
			if (metadata.isMultivalue()) {
				convertedRecord = EnumWithSmallCodeUtils.toSmallCodeList((List<Enum<?>>) value);
			} else {
				convertedRecord = EnumWithSmallCodeUtils.toSmallCode((Enum<?>) value);
			}

		} else if (value instanceof List) {
			List<Object> convertedRecordList = new ArrayList<>();
			for (Object item : (List) value) {
				if (item instanceof Record) {
					convertedRecordList.add(((Record) item).getId());
				} else if (item instanceof RecordWrapper) {
					convertedRecordList.add(((RecordWrapper) item).getWrappedRecord().getId());
				} else {
					convertedRecordList.add(item);
				}
			}
			convertedRecord = convertedRecordList;
		} else {
			convertedRecord = value;
		}

		return setModifiedValue(metadata, language, convertedRecord);
	}

	private void validateScalarValue(Metadata metadata, Object value) {
		if (metadata.getType() == MetadataValueType.INTEGER || metadata.getType() == MetadataValueType.NUMBER) {
			if (value != null && !(value instanceof Number)) {
				throw new IllegalArgumentException("Invalid value for integer/number metadata : " + value.getClass().getName());
			}
		} else if (metadata.getType() == MetadataValueType.STRING || metadata.getType() == MetadataValueType.TEXT) {
			if (value != null && !(value instanceof String)) {
				throw new IllegalArgumentException("Invalid value for string/text metadata : " + value.getClass().getName());
			}

		} else if (metadata.getType() == MetadataValueType.REFERENCE) {
			if (value != null && !(value instanceof String) && !(value instanceof Record) && !(value instanceof RecordWrapper)) {
				throw new IllegalArgumentException("Invalid value for reference metadata : " + value.getClass().getName());
			}
		}
	}

	private void validateMetadata(Metadata metadata) {
		if (metadata == null) {
			throw new RequiredMetadataArgument();
		}
		String code = metadata.getCode();
		if (code == null) {
			throw new InvalidMetadata("null");
		}
		if (code.startsWith("global_default")) {
			return;
		}
		if (!code.startsWith(schemaCode) && !code.startsWith(schemaTypeCode + "_default")) {
			throw new InvalidMetadata(code);
		}

		if (metadata.getDataEntry().getType() == CALCULATED &&
			!((CalculatedDataEntry) metadata.getDataEntry()).getCalculator().hasEvaluator()) {
			throw new RecordRuntimeException.CannotSetManualValueInAutomaticField(metadata);
		} else if (metadata.getDataEntry().getType() != CALCULATED &&
				   metadata.getDataEntry().getType() != MANUAL &&
				   metadata.getDataEntry().getType() != SEQUENCE) {
			throw new RecordRuntimeException.CannotSetManualValueInAutomaticField(metadata);
		}

		/*if (metadata.getDataEntry().getType() != MANUAL && metadata.getDataEntry().getType() != SEQUENCE) {
			throw new RecordRuntimeException.CannotSetManualValueInAutomaticField(metadata);
		}*/

		if (metadata.getLocalCode().equals("id")) {
			throw new RecordRuntimeException_CannotModifyId();
		}
	}

	private Record setModifiedValue(Metadata metadata, String language, Object value) {
		validateSetArguments(metadata, value);

		Map<String, Object> map = modifiedValues;
		if (metadata.getTransiency() == MetadataTransiency.TRANSIENT_EAGER) {
			map = eagerTransientValues;

		} else if (metadata.getTransiency() == MetadataTransiency.TRANSIENT_LAZY) {
			map = lazyTransientValues;
		}

		Object correctedValue = correctValue(value);
		String codeAndType;

		if (language == null || collectionInfo.getMainSystemLanguage().getCode().equals(language)) {
			codeAndType = metadata.getDataStoreCode();
		} else {
			codeAndType = metadata.getSecondaryLanguageDataStoreCode(language);
		}
		if (structuredValues != null && structuredValues.containsKey(codeAndType)) {
			if (!structuredValues.get(codeAndType).equals(correctedValue)) {
				map.put(codeAndType, correctedValue);
			} else {
				map.remove(codeAndType);
			}
		} else {

			if (!isSameValueThanDTO(metadata, correctedValue, codeAndType)) {
				map.put(codeAndType, correctedValue);
			} else {
				map.remove(codeAndType);
			}
		}

		return this;
	}

	private boolean isSameValueThanDTO(Metadata metadata, Object value, String codeAndType) {
		boolean sameAsDTOValue = false;
		if (recordDTO != null) {
			Object dtoValue = recordDTO.getFields().get(codeAndType);
			if (value instanceof List && ((List) value).isEmpty()) {

				sameAsDTOValue = dtoValue == null || (dtoValue instanceof List && ((List) dtoValue).isEmpty());
			} else {

				sameAsDTOValue = Objects.equals(dtoValue, value);
			}
		}
		return sameAsDTOValue;
	}

	private void validateSetArguments(Metadata metadata, Object value) {
		if (disconnected) {
			throw new RecordRuntimeException.CannotModifyADisconnectedRecord(id);
		}
		if (metadata == null) {
			throw new RecordRuntimeException.RequiredMetadataArgument();
		}
		if (value == null) {
			return;
		}
		if (metadata.isMultivalue() && !(value instanceof List)) {
			throw new RecordRuntimeException.CannotSetNonListValueInMultivalueMetadata(metadata, value.getClass());
		}
		if (!metadata.isMultivalue() && (value instanceof Collection)) {
			throw new RecordRuntimeException.CannotSetCollectionInSingleValueMetadata(metadata);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Metadata metadata, Locale locale) {
		return get(metadata, locale == null ? collectionInfo.getMainSystemLocale().getLanguage() : locale.getLanguage(),
				PREFERRING);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Metadata metadata, Locale locale, LocalisedRecordMetadataRetrieval mode) {
		return get(metadata, locale == null ? collectionInfo.getMainSystemLanguage().getCode() : locale.getLanguage(), mode);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Metadata metadata) {
		return get(metadata, collectionInfo.getMainSystemLanguage().getCode(), STRICT);
	}

	private <T> T get(Metadata metadata, String language, LocalisedRecordMetadataRetrieval mode) {

		if (metadata == null) {
			throw new RecordRuntimeException.RequiredMetadataArgument();
		}
		if (Schemas.IDENTIFIER.getLocalCode().equals(metadata.getLocalCode())) {
			return (T) id;
		}

		String codeAndType;


		if (recordDTO != null && recordDTO.getLoadingMode() == RecordDTOMode.SUMMARY && !SchemaUtils.isSummary(metadata)) {
			throw new IllegalArgumentException("Non summary metadata '" + metadata.getCode() + "' cannot be obtained on summary record");
		}

		if (collectionInfo.getMainSystemLanguage().getCode().equals(language) || language == null) {
			codeAndType = metadata.getDataStoreCode();
		} else {
			codeAndType = metadata.getSecondaryLanguageDataStoreCode(language);
		}

		T returnedValue;
		if (metadata.getTransiency() == MetadataTransiency.TRANSIENT_LAZY) {
			returnedValue = (T) lazyTransientValues.get(codeAndType);

		} else if (metadata.getTransiency() == MetadataTransiency.TRANSIENT_EAGER) {
			returnedValue = (T) eagerTransientValues.get(codeAndType);

		} else if (modifiedValues.containsKey(codeAndType)) {
			returnedValue = (T) modifiedValues.get(codeAndType);

		} else if (recordDTO != null) {
			Object value = recordDTO.getFields().get(codeAndType);

			returnedValue = (T) getConvertedValue(value, metadata);

		} else {
			returnedValue = null;
		}

		String mainDataLanguage = collectionInfo.getMainSystemLanguage().getCode();
		if (mode == PREFERRING && LangUtils.isNullOrEmptyCollection(returnedValue) && !language.equals(mainDataLanguage)) {
			returnedValue = get(metadata, mainDataLanguage, STRICT);
		}

		if (metadata.getEnumClass() != null && returnedValue != null) {
			if (metadata.isMultivalue()) {
				List<Object> converted = new ArrayList<>();

				for (Object item : (List) returnedValue) {
					converted.add((T) convertEnumValue(metadata, item));
				}
				returnedValue = (T) converted;
			} else {
				returnedValue = convertEnumValue(metadata, returnedValue);
			}
		}

		if (metadata.isMultivalue()) {
			if (returnedValue == null) {
				returnedValue = (T) unmodifiableList(Collections.emptyList());
			} else {
				returnedValue = (T) unmodifiableList((List<? extends T>) returnedValue);
			}
		}

		return returnedValue;
	}

	private <T> T convertEnumValue(Metadata metadata, T returnedValue) {
		if (returnedValue instanceof String) {
			returnedValue = (T) EnumWithSmallCodeUtils.toEnum(metadata.getEnumClass(), (String) returnedValue);
		} else if (returnedValue instanceof EnumWithSmallCode) {
			returnedValue = returnedValue;
		} else {
			throw new ImpossibleRuntimeException(
					"Unsupported value of type '" + returnedValue.getClass().getName() + "' in enum metadata");
		}
		return returnedValue;
	}

	private Object getConvertedValue(Object rawValue, Metadata metadata) {

		if (!isConvertedValue(metadata)) {

			if (metadata.getType() == MetadataValueType.DATE && "".equals(rawValue)) {
				return null;
			}

			return rawValue;
		}

		if (rawValue == null) {
			return null;
		}

		if (metadata.isEncrypted()) {
			EncryptionServices encryptionServices = metadata.getEncryptionServicesFactory().get();
			return encryptionServices.decrypt(rawValue);
		}

		if (structuredValues == null) {
			structuredValues = new HashMap<>();
		}

		Object convertedValue = structuredValues.get(metadata.getDataStoreCode());
		if (convertedValue == null) {
			try {
				convertedValue = convertToStructuredValue(rawValue, metadata);
			} catch (RecordImplException_CannotBuildStructureValue e) {
				LOGGER.error("Error while building a structure value", e);
				convertedValue = null;
			}
			structuredValues.put(metadata.getDataStoreCode(), convertedValue);
		}
		return convertedValue;
	}

	private Object convertToStructuredValue(Object rawValue, Metadata metadata) {
		if (rawValue instanceof List) {
			List<Object> convertedValues = new ArrayList<>();
			for (Object value : (List) rawValue) {
				if (value == null) {
					convertedValues.add(null);
				} else {
					convertedValues.add(convertToStructuredValue(value, metadata));
				}
			}
			return convertedValues;
		} else {
			try {
				return metadata.getStructureFactory().build((String) rawValue);
			} catch (RuntimeException e) {
				throw new RecordImplException_CannotBuildStructureValue(id, (String) rawValue, e);
			}
		}
	}

	private Object convertStructuredValueToString(Object structureValue, Metadata metadata) {
		if (structureValue instanceof List) {
			List<Object> convertedValues = new ArrayList<>();
			for (Object value : (List) structureValue) {
				convertedValues.add(convertStructuredValueToString(value, metadata));
			}
			return convertedValues;
		} else {
			ModifiableStructure structure = (ModifiableStructure) structureValue;
			return structure == null ? null : metadata.getStructureFactory().toString(structure);
		}
	}

	private boolean isConvertedValue(Metadata metadata) {
		return metadata.getStructureFactory() != null || metadata.isEncrypted();
	}

	@Override
	public <T> T getNonNullValueIn(List<Metadata> metadatas) {
		T nonNullValue = null;
		for (Metadata metadata : metadatas) {
			Object value = get(metadata);
			if (value != null) {
				if (nonNullValue == null) {
					nonNullValue = (T) value;
				} else {
					throw new RecordImplException_RecordCannotHaveTwoParents(id);
				}
			}
		}

		return nonNullValue;
	}

	@Override
	public <T> List<T> getList(Metadata metadata) {
		Object value = get(metadata);
		if (value == null) {
			return Collections.emptyList();
		} else {
			if (metadata.isMultivalue()) {
				return (List<T>) value;
			} else {
				throw new CannotGetListForSingleValue(metadata.getLocalCode());
			}
		}
	}

	@Override
	public <T> List<T> getList(Metadata metadata, Locale locale, LocalisedRecordMetadataRetrieval mode) {
		Object value = get(metadata, locale, mode);
		if (value == null) {
			return Collections.emptyList();
		} else {
			if (metadata.isMultivalue()) {
				return (List<T>) value;
			} else {
				throw new CannotGetListForSingleValue(metadata.getLocalCode());
			}
		}
	}

	public <T> List<T> getValues(Metadata metadata) {
		Object value = get(metadata);
		if (value == null) {
			return Collections.emptyList();
		} else {
			if (metadata.isMultivalue()) {
				return (List<T>) value;
			} else {
				List<T> values = Collections.singletonList((T) value);
				return values;
			}
		}
	}

	public <T> List<T> getValues(Metadata metadata, Locale locale, LocalisedRecordMetadataRetrieval mode) {
		Object value = get(metadata, locale, mode);
		if (value == null) {
			return Collections.emptyList();
		} else {
			if (metadata.isMultivalue()) {
				return (List<T>) value;
			} else {
				List<T> values = asList((T) value);
				return values;
			}
		}
	}

	public void refresh(long version, RecordDTO recordDTO) {
		if (recordDTO == null) {
			throw new RecordRuntimeException.RecordDTORequired();
		}
		if (recordDTO.getFields().get("collection_s") == null) {
			throw new IllegalArgumentException("Argument recordDTO requires a collection_s value");
		}
		if (recordDTO.getFields().get("schema_s") == null) {
			throw new IllegalArgumentException("Argument recordDTO requires a schema_s value");
		}
		ensureModifiable();

		this.version = version;
		this.recordDTO = recordDTO;
		this.unmodifiableCopyOfOriginalRecord = null;
		this.modifiedValues.clear();
		if (structuredValues != null) {
			this.structuredValues.clear();
		}
	}

	private void ensureModifiable() {
		if (unmodifiable) {
			throw new RecordImplRuntimeException.RecordImplException_RecordIsUnmodifiable();
		}

	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long getVersion() {
		return version;
	}

	@Override
	public long getDataMigrationVersion() {
		Double value = get(Schemas.MIGRATION_DATA_VERSION);
		return value == null ? 0 : value.longValue();
	}

	@Override
	public String getSchemaCode() {
		return schemaCode;
	}

	@Override
	public String getTypeCode() {
		return schemaTypeCode;
	}

	public RecordDTO getRecordDTO() {
		return recordDTO;
	}

	public Map<String, Object> getModifiedValues() {
		addDirtyStructuresToModifiedMap();
		return Collections.unmodifiableMap(modifiedValues);
	}

	public void markAsDisconnected() {
		this.disconnected = true;
	}

	@Override
	public boolean isDirty() {
		addDirtyStructuresToModifiedMap();
		for (String modifiedMetadata : modifiedValues.keySet()) {
			if (!isCreationOrModificationInfo(modifiedMetadata)) {
				return true;
			}
		}
		return false;
	}

	private void addDirtyStructuresToModifiedMap() {
		if (structuredValues != null) {
			for (Map.Entry<String, Object> structureMetadataKeyValue : structuredValues.entrySet()) {
				Object structureMetadataValue = structureMetadataKeyValue.getValue();
				String structureMetadataKey = structureMetadataKeyValue.getKey();
				if (!modifiedValues.containsKey(structureMetadataKey)) {
					if (structureMetadataValue != null && isDirtyStructuredValue(structureMetadataValue)) {
						modifiedValues.put(structureMetadataKey, structureMetadataValue);
					}
				}
			}
		}
	}

	private boolean isDirtyStructuredValue(Object value) {
		if (value instanceof List) {
			for (Object structureMetadataValueItem : (List) value) {
				ModifiableStructure modifiableStructure = (ModifiableStructure) structureMetadataValueItem;
				if (modifiableStructure != null && modifiableStructure.isDirty()) {
					return true;
				}
			}
			return false;
		} else {
			ModifiableStructure modifiableStructure = (ModifiableStructure) value;
			return modifiableStructure.isDirty();
		}

	}

	public RecordDTO toNewDocumentDTO(MetadataSchema schema, List<FieldsPopulator> copyfieldsPopulators) {

		if (version != -1) {
			throw new RecordIsAlreadySaved(id);
		}
		return toDocumentDTO(schema, copyfieldsPopulators);
	}

	public RecordDTO toDocumentDTO(MetadataSchema schema, List<FieldsPopulator> copyfieldsPopulators) {

		Map<String, Object> fields = new HashMap<String, Object>();

		RecordDTOMode mode = RecordDTOMode.FULLY_LOADED;
		if (recordDTO != null) {
			fields.putAll(recordDTO.getFields());
			mode = recordDTO.getLoadingMode();
		}


		for (Map.Entry<String, Object> entry : modifiedValues.entrySet()) {
			String metadataAtomicCode = new SchemaUtils().getLocalCodeFromDataStoreCode(entry.getKey());
			Metadata metadata = schema.getMetadata(metadataAtomicCode);
			if (metadata.getTransiency() == MetadataTransiency.PERSISTED || metadata.getTransiency() == null) {
				Object value = entry.getValue();

				if (metadata.isEncrypted() && value != null) {
					EncryptionServices encryptionServices = metadata.getEncryptionServicesFactory().get();
					fields.put(entry.getKey(), encryptionServices.encrypt(value));

				} else if (metadata.getStructureFactory() != null) {
					fields.put(entry.getKey(), convertStructuredValueToString(value, metadata));

				} else {
					fields.put(entry.getKey(), value);
				}
			}
		}

		Map<String, Object> copyfields = new HashMap<>();
		for (FieldsPopulator populator : copyfieldsPopulators) {
			for (Map.Entry<String, Object> entry : populator.populateCopyfields(schema, this).entrySet()) {
				if (entry.getValue() == null) {
					throw new RecordImplException_PopulatorReturnedNullValue(populator, entry.getKey());
				}
				copyfields.put(entry.getKey(), entry.getValue());
			}
		}

		for (String copiedKey : copyfields.keySet()) {
			fields.remove(copiedKey);
		}

		fields.remove("_version_");
		fields.put("schema_s", schemaCode);
		fields.put("collection_s", collection);
		fields.put("estimatedSize_i", RecordUtils.estimateRecordSize(fields, copyfields));

		return lastCreatedRecordDTO = new SolrRecordDTO(id, version, fields, copyfields, mode);

	}

	@Override
	public MetadataList getModifiedMetadatas(MetadataSchemaTypes schemaTypes) {
		MetadataList modifiedMetadatas = new MetadataList();

		for (String modifiedMetadataDataStoreCode : getModifiedValues().keySet()) {
			String localCode = SchemaUtils.underscoreSplitWithCache(modifiedMetadataDataStoreCode)[0];

			try {

				Metadata metadata = schemaTypes.getSchema(schemaCode).getMetadata(localCode);

				boolean modified;
				if (metadata.isMultivalue()) {
					List<Object> currentValues = getList(metadata);
					List<Object> originalValues;
					if (isSaved()) {
						originalValues = getUnmodifiableCopyOfOriginalRecord().getList(metadata);
					} else {
						originalValues = new ArrayList<>();
					}

					modified = ObjectUtils.notEqual(originalValues, currentValues);
				} else {

					if (metadata.getType() == MetadataValueType.NUMBER) {
						Double currentDoubleValue = get(metadata);
						if (new Double(0.0).equals(currentDoubleValue)) {
							currentDoubleValue = null;
						}

						Double originalDoubleValue;
						if (isSaved()) {
							originalDoubleValue = getUnmodifiableCopyOfOriginalRecord().get(metadata);
							if (new Double(0.0).equals(originalDoubleValue)) {
								originalDoubleValue = null;
							}
						} else {
							originalDoubleValue = null;
						}

						modified = ObjectUtils.notEqual(originalDoubleValue, currentDoubleValue);
					} else {
						Object currentValue = get(metadata);
						Object originalValue = isSaved() ? getUnmodifiableCopyOfOriginalRecord().get(metadata) : null;
						modified = ObjectUtils.notEqual(currentValue, originalValue);
					}
				}

				if (modified) {
					modifiedMetadatas.add(metadata);
				}
			} catch (NoSuchMetadata e) {
				if (isSaved()) {
					Record originalRecord = getCopyOfOriginalRecord();
					try {
						modifiedMetadatas.add(schemaTypes.getSchema(originalRecord.getSchemaCode()).getMetadata(localCode));
					} catch (NoSuchMetadata e2) {

					}
				}
			}
		}

		return modifiedMetadatas.unModifiable();
	}

	public RecordDeltaDTO toRecordDeltaDTO(MetadataSchema schema, List<FieldsPopulator> copyfieldsPopulators) {

		Map<String, Object> modifiedValues = getModifiedValues();
		Map<String, Object> convertedValues = new HashMap<>(modifiedValues);
		Map<String, Object> copyfields = new HashMap<>();

		for (FieldsPopulator populator : copyfieldsPopulators) {
			for (Map.Entry<String, Object> entry : populator.populateCopyfields(schema, this).entrySet()) {
				copyfields.put(entry.getKey(), entry.getValue());
			}
		}

		for (Map.Entry<String, Object> entry : modifiedValues.entrySet()) {
			String localCode = new SchemaUtils().getLocalCodeFromDataStoreCode(entry.getKey());
			try {
				Metadata metadata = schema.getMetadata(localCode);
				if (metadata.getTransiency() == MetadataTransiency.PERSISTED || metadata.getTransiency() == null) {
					Object value = entry.getValue();

					if (metadata.isEncrypted() && value != null) {
						EncryptionServices encryptionServices = metadata.getEncryptionServicesFactory().get();
						convertedValues.put(entry.getKey(), encryptionServices.encrypt(value));
					}

					if (metadata.getStructureFactory() != null) {
						convertedValues.put(entry.getKey(), convertStructuredValueToString(value, metadata));
					}
				}
			} catch (NoSuchMetadata e) {
				convertedValues.put(entry.getKey(), null);
			}

		}

		if (!schemaCode.equals(recordDTO.getFields().get("schema_s"))) {
			convertedValues.put("schema_s", schemaCode);
		}

		Integer currentSize = (Integer) recordDTO.getFields().get("estimatedSize_i");
		if (currentSize == null) {
			currentSize = RecordUtils.estimateRecordSize(recordDTO.getFields(), recordDTO.getCopyFields());
		}

		int estimatedSizeDelta = estimateRecordUpdateSize(
				convertedValues, recordDTO.getFields(), copyfields, recordDTO.getCopyFields());
		convertedValues.put("estimatedSize_i", currentSize + estimatedSizeDelta);

		return lastCreatedDeltaDTO = new RecordDeltaDTO(id, version, convertedValues, recordDTO.getFields(), copyfields);
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RecordImpl)) {
			return false;
		}

		RecordImpl record = (RecordImpl) o;

		if (version != record.version) {
			return false;
		}
		if (disconnected != record.disconnected) {
			return false;
		}
		if (modifiedValues != null ? !modifiedValues.equals(record.modifiedValues) : record.modifiedValues != null) {
			return false;
		}
		if (schemaCode != null ? !schemaCode.equals(record.schemaCode) : record.schemaCode != null) {
			return false;
		}
		if (collection != null ? !collection.equals(record.collection) : record.collection != null) {
			return false;
		}
		if (!id.equals(record.id)) {
			return false;
		}

		if (this.structuredValues != null) {
			for (Map.Entry<String, Object> entry : this.structuredValues.entrySet()) {
				if (entry.getValue() instanceof ModifiableStructure) {
					if (((ModifiableStructure) entry.getValue()).isDirty()) {

						if (record.structuredValues == null || !entry.getValue().equals(
								record.structuredValues.get(entry.getKey()))) {
							return false;

						}
					}
				}
			}
		}

		if (record.structuredValues != null) {
			for (Map.Entry<String, Object> entry : record.structuredValues.entrySet()) {
				if (entry.getValue() instanceof ModifiableStructure) {
					if (((ModifiableStructure) entry.getValue()).isDirty()) {
						if (this.structuredValues == null || !entry.getValue().equals(
								this.structuredValues.get(entry.getKey()))) {
							return false;

						}
					}
				}
			}
		}


		return true;
	}

	@Override
	public int hashCode() {
		int result = modifiedValues != null ? modifiedValues.hashCode() : 0;
		result = 31 * result + (schemaCode != null ? schemaCode.hashCode() : 0);
		result = 31 * result + (collection != null ? collection.hashCode() : 0);
		result = 31 * result + id.hashCode();
		result = 31 * result + (int) (version ^ (version >>> 32));
		result = 31 * result + (disconnected ? 1 : 0);
		result = 31 * result + (structuredValues != null ? structuredValues.hashCode() : 0);
		return result;
	}

	@Override
	public boolean isModified(Metadata metadata) {
		return getModifiedValues().containsKey(metadata.getDataStoreCode());
	}

	@Override
	public boolean isSaved() {
		return version != -1;
	}

	public void merge(RecordImpl otherVersion, MetadataSchema schema) {
		ensureModifiable();
		RecordDTO otherVersionRecordDTO = otherVersion.recordDTO;

		List<String> removedKeys = new ArrayList<>();
		for (Entry<String, Object> entry : modifiedValues.entrySet()) {
			String key = entry.getKey();
			boolean specialField = key.equals("schema_s") || key.equals("id") || key.equals("_version_")
								   || key.equals("autocomplete_ss") || key.equals("collection_s")
								   || key.equals("modifiedOn_dt") || key.equals("createdOn_dt") || key.equals("modifiedById_s");

			if (!specialField) {
				String metadataCode = new SchemaUtils().getLocalCodeFromDataStoreCode(key);
				if (!specialField && schema.getMetadata(metadataCode).getDataEntry().getType() == MANUAL) {
					Object initialValue = recordDTO.getFields().get(entry.getKey());
					Object modifiedValue = entry.getValue();
					Object currentValue = otherVersionRecordDTO.getFields().get(entry.getKey());
					if (LangUtils.areNullableEqual(currentValue, modifiedValue)
						|| (currentValue == null && isEmptyList(modifiedValue))
						|| (modifiedValue == null && isEmptyList(currentValue))) {
						//Both transactions made the same change on that field
						removedKeys.add(entry.getKey());
					} else {

						if (!(LangUtils.areNullableEqual(currentValue, initialValue)
							  || (currentValue == null && isEmptyList(initialValue))
							  || (initialValue == null && isEmptyList(currentValue)))) {
							throw new RecordRuntimeException.CannotMerge(schema.getCode(), id, key, currentValue, initialValue);
						}
					}
				}
			}
		}
		for (String removedKey : removedKeys) {
			modifiedValues.remove(removedKey);
		}

		this.version = otherVersion.getVersion();
		this.recordDTO = otherVersion.getRecordDTO();
		this.unmodifiableCopyOfOriginalRecord = null;
	}

	private boolean isCreationOrModificationInfo(String key) {
		return Schemas.MODIFIED_BY.getDataStoreCode().equals(key) || Schemas.MODIFIED_ON.getDataStoreCode().equals(key)
			   || Schemas.CREATED_BY.getDataStoreCode().equals(key) || Schemas.CREATED_ON.getDataStoreCode().equals(key);
	}

	private Object correctValue(Object value) {
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else if (value instanceof EnumWithSmallCode) {
			return ((EnumWithSmallCode) value).getCode();
		}
		return value;
	}

	public void markAsSaved(long version, MetadataSchema schema) {
		ensureModifiable();
		if (!isSaved()) {
			if (lastCreatedRecordDTO == null) {
				//This should never happen
				lastCreatedRecordDTO = toNewDocumentDTO(schema, new ArrayList<FieldsPopulator>()).withVersion(version);
			}
			RecordDTO dto = lastCreatedRecordDTO.withVersion(version);
			refresh(version, dto);

		} else {
			if (lastCreatedDeltaDTO == null) {
				//This should never happen
				lastCreatedDeltaDTO = toRecordDeltaDTO(schema, new ArrayList<FieldsPopulator>());
			}

			RecordDTO dto = recordDTO.createCopyWithDelta(lastCreatedDeltaDTO).withVersion(version);
			refresh(version, dto);
		}

		lastCreatedRecordDTO = null;
		lastCreatedDeltaDTO = null;

	}

	@Override
	public RecordDTOMode getRecordDTOMode() {
		return recordDTO == null ? RecordDTOMode.FULLY_LOADED : recordDTO.getLoadingMode();
	}

	@Override
	public String getCollection() {
		return collection;
	}

	@Override
	public CollectionInfo getCollectionInfo() {
		return collectionInfo;
	}

	@Override
	public String getParentId() {

		for (Map.Entry<String, Object> entry : modifiedValues.entrySet()) {
			if (entry.getKey().contains("PId_") && entry.getValue() != null) {
				return (String) entry.getValue();
			}
		}

		if (recordDTO != null) {
			for (Map.Entry<String, Object> entry : recordDTO.getFields().entrySet()) {
				if (entry.getKey().contains("PId_") && entry.getValue() != null) {
					return (String) entry.getValue();
				}
			}
		}
		return null;
	}

	public Map<String, Object> getLoadedStructuredValues() {
		return structuredValues;
	}

	@Override
	public boolean isActive() {
		return !Boolean.TRUE.equals(get(Schemas.LOGICALLY_DELETED_STATUS));
	}

	@Override
	public boolean isDisconnected() {
		return disconnected;
	}

	@Override
	public Record getCopyOfOriginalRecord() {
		if (recordDTO == null) {
			throw new RecordImplException_UnsupportedOperationOnUnsavedRecord("getCopyOfOriginalRecord", id);
		}
		return new RecordImpl(recordDTO, collectionInfo, eagerTransientValues);
	}

	@Override
	public Record getUnmodifiableCopyOfOriginalRecord() {
		if (recordDTO == null) {
			throw new RecordImplException_UnsupportedOperationOnUnsavedRecord("getCopyOfOriginalRecord", id);
		}

		if (unmodifiableCopyOfOriginalRecord == null) {
			unmodifiableCopyOfOriginalRecord = new RecordImpl(recordDTO, collectionInfo, eagerTransientValues, true);
		}
		return unmodifiableCopyOfOriginalRecord;
	}

	@Override
	@Deprecated
	public Record getCopyOfOriginalRecordKeepingOnly(List<Metadata> metadatas) {
		if (recordDTO == null) {
			throw new RecordImplException_UnsupportedOperationOnUnsavedRecord("getCopyOfOriginalRecord", id);
		}

		Set<String> metadatasDataStoreCodes = new HashSet<>();
		for (Metadata metadata : metadatas) {
			metadatasDataStoreCodes.add(metadata.getDataStoreCode());
			for (String collectionLanguageCode : collectionInfo.getSecondaryCollectionLanguesCodes()) {
				metadatasDataStoreCodes.add(metadata.getSecondaryLanguageDataStoreCode(collectionLanguageCode));
			}
		}

		Map<String, Object> newEagerTransientValues = new HashMap<>();
		for (Map.Entry<String, Object> eagerTransientValue : eagerTransientValues.entrySet()) {
			if (metadatasDataStoreCodes.contains(eagerTransientValue.getKey())) {
				newEagerTransientValues.put(eagerTransientValue.getKey(), eagerTransientValue.getValue());
			}
		}

		return new RecordImpl(recordDTO.createCopyOnlyKeeping(metadatasDataStoreCodes), collectionInfo, newEagerTransientValues,
				false);
	}

	@Override
	public String getIdTitle() {
		String title = getTitle();
		return id + (title == null ? "" : (":" + title));
	}

	@Override
	public String getSchemaIdTitle() {
		return "(" + getSchemaCode() + ") " + getTitle();
	}

	@Override
	public String getTitle() {
		return get(Schemas.TITLE);
	}

	@Override
	public void removeAllFieldsStartingWith(String prefix) {
		if (recordDTO != null) {
			for (String entry : recordDTO.getFields().keySet()) {
				if (entry.startsWith(prefix)) {
					modifiedValues.put(entry, null);
				}
			}
			for (String entry : recordDTO.getCopyFields().keySet()) {
				if (entry.startsWith(prefix)) {
					modifiedValues.put(entry, null);
				}
			}
		}
	}

	@Override
	public void markAsModified(Metadata metadata) {
		ensureModifiable();
		modifiedValues.put(metadata.getDataStoreCode(), get(metadata));
	}


	@Override
	public boolean changeSchema(MetadataSchema wasSchema, MetadataSchema newSchema) {
		ensureModifiable();
		boolean lostMetadataValues = false;
		Map<String, Metadata> newSchemasMetadatas = new HashMap<>();
		for (Metadata metadata : newSchema.getMetadatas()) {
			newSchemasMetadatas.put(metadata.getLocalCode(), metadata);
		}
		for (Metadata wasMetadata : wasSchema.getMetadatas()) {
			if (wasMetadata.getDataEntry().getType() == MANUAL) {
				Metadata newMetadata = newSchemasMetadatas.get(wasMetadata.getLocalCode());
				if (newMetadata == null || !newMetadata.isSameValueThan(wasMetadata)) {

					Object value = get(wasMetadata);
					if (!(value == null || isBlankString(value) || isEmptyList(value) || isDefaultValue(value, wasMetadata))) {
						lostMetadataValues = true;
					}
					set(wasMetadata, null);

				} else {
					Object value = get(wasMetadata);
					if (value == null || isBlankString(value) || isEmptyList(value) || isDefaultValue(value, wasMetadata)) {
						set(wasMetadata, null);
					}
				}
			}
		}

		this.schemaCode = newSchema.getCode();

		for (Metadata metadata : newSchema.getMetadatas()) {
			if (metadata.getDataEntry().getType() == MANUAL) {
				if (metadata.isMultivalue()) {
					List<Object> value = getList(metadata);
					if (value.isEmpty()) {
						set(metadata, metadata.getDefaultValue());
					}
				} else {
					Object value = get(metadata);
					if (value == null) {
						set(metadata, metadata.getDefaultValue());
					}
				}
			}
		}
		markAsModified(Schemas.SCHEMA);

		return lostMetadataValues;
	}

	@Override
	public boolean isOfSchemaType(String type) {
		return schemaCode.startsWith(type + "_");
	}

	private static boolean isDefaultValue(Object value, Metadata metadata) {
		if (metadata.isMultivalue()) {
			Object defaultValue = metadata.getDefaultValue();
			if (defaultValue == null || isEmptyList(defaultValue)) {
				return isEmptyList(value);
			} else {
				List<Object> defaultListValues = new ArrayList<>((List<?>) metadata.getDefaultValue());

				if (isEmptyList(value)) {
					return false;
				} else if (value instanceof List) {
					List<?> listValue = new ArrayList<>((List<?>) value);
					return listValue.equals(defaultListValues);
				} else {
					return false;
				}

			}

		} else {
			return LangUtils.isEqual(value, metadata.getDefaultValue());
		}
	}

	public static boolean isListWithSameContentEqual(Object value1, Object value2) {
		if (value1 == null) {
			return value2 == null;
		} else {
			if (value2 == null) {
				return false;
			} else {
				if (value1 instanceof List) {
					;
				}

			}

			return value1.equals(value2);
		}
	}

	private static boolean isEmptyList(Object value) {
		return (value instanceof List) && ((List) value).isEmpty();
	}

	private boolean isBlankString(Object value) {
		return (value instanceof String) && StringUtils.isBlank((String) value);
	}

	public Map<String, Object> getLazyTransientValues() {
		return lazyTransientValues;
	}

	public Map<String, Object> getEagerTransientValues() {
		return eagerTransientValues;
	}

	@Override
	public <T> void addValueToList(Metadata metadata, T value) {
		ensureModifiable();
		List<T> values = new ArrayList<>(this.<T>getList(metadata));
		values.add(value);
		set(metadata, values);
	}

	@Override
	public <T> void removeValueFromList(Metadata metadata, T value) {
		ensureModifiable();
		List<T> values = new ArrayList<>(this.<T>getList(metadata));
		values.remove(value);
		set(metadata, values);
	}

	@Override
	public Record get() {
		return this;
	}
}
