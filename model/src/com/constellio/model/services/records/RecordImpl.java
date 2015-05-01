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
package com.constellio.model.services.records;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordRuntimeException;
import com.constellio.model.entities.records.RecordRuntimeException.InvalidMetadata;
import com.constellio.model.entities.records.RecordRuntimeException.RecordIsAlreadySaved;
import com.constellio.model.entities.records.RecordRuntimeException.RequiredMetadataArgument;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.records.RecordImplRuntimeException.CannotGetListForSingleValue;
import com.constellio.model.services.records.RecordImplRuntimeException.RecordImplException_PopulatorReturnedNullValue;
import com.constellio.model.services.records.RecordImplRuntimeException.RecordImplException_RecordCannotHaveTwoParents;
import com.constellio.model.services.records.RecordImplRuntimeException.RecordImplException_UnsupportedOperationOnUnsavedRecord;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class RecordImpl implements Record {

	protected final Map<String, Object> modifiedValues = new HashMap<String, Object>();
	private final String schemaCode;
	private final String collection;
	private final String id;
	private long version;
	private boolean disconnected = false;
	private RecordDTO recordDTO;
	private Map<String, Object> structuredValues;
	private List<String> followers;

	public RecordImpl(String schemaCode, String collection, String id) {
		if (schemaCode == null) {
			throw new IllegalArgumentException("Require schema code");
		}
		if (collection == null) {
			throw new IllegalArgumentException("Require collection code");
		}
		this.collection = collection;

		this.id = id;
		this.schemaCode = schemaCode;
		this.version = -1;
		this.recordDTO = null;
		this.followers = new ArrayList<String>();
	}

	public RecordImpl(RecordDTO recordDTO) {

		this.id = recordDTO.getId();
		this.version = recordDTO.getVersion();
		this.schemaCode = (String) recordDTO.getFields().get("schema_s");
		this.collection = (String) recordDTO.getFields().get("collection_s");
		if (collection == null) {
			throw new IllegalArgumentException("Require collection code");
		}

		this.followers = (List<String>) recordDTO.getFields().get("followers_ss");
		if (this.followers == null) {
			this.followers = new ArrayList<>();
		}
		this.recordDTO = recordDTO;
	}

	public Record updateAutomaticValue(Metadata metadata, Object value) {

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
			return setModifiedValue(metadata, Collections.unmodifiableList((List<?>) convertedRecord));
		} else {
			return setModifiedValue(metadata, convertedRecord);
		}
	}

	@Override
	public Record set(Metadata metadata, Object value) {

		// Get may parse some metadata, and this is required later
		get(metadata);
		validateMetadata(metadata);

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

		return setModifiedValue(metadata, convertedRecord);
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
		if (!code.startsWith(schemaCode)) {
			throw new InvalidMetadata(code);
		}
		if (metadata.getDataEntry().getType() != DataEntryType.MANUAL) {
			throw new RecordRuntimeException.CannotSetManualValueInAutomaticField(metadata);
		}
	}

	private Record setModifiedValue(Metadata metadata, Object value) {
		validateSetArguments(metadata, value);

		Object correctedValue = correctValue(value);
		String codeAndType = metadata.getDataStoreCode();
		if (!isSameValueThanDTO(correctedValue, codeAndType)) {
			modifiedValues.put(codeAndType, correctedValue);
		} else {
			modifiedValues.remove(codeAndType);
		}
		return this;
	}

	private boolean isSameValueThanDTO(Object value, String codeAndType) {
		boolean sameAsDTOValue = false;
		if (recordDTO != null) {
			sameAsDTOValue = Objects.equals(recordDTO.getFields().get(codeAndType), value);
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
	public <T> T get(Metadata metadata) {
		if (metadata == null) {
			throw new RecordRuntimeException.RequiredMetadataArgument();
		}
		String codeAndType = metadata.getDataStoreCode();
		T returnedValue;
		if (modifiedValues.containsKey(codeAndType)) {
			returnedValue = (T) modifiedValues.get(codeAndType);

		} else if (recordDTO != null) {
			returnedValue = (T) getConvertedValue(recordDTO.getFields().get(codeAndType), metadata);

		} else {
			returnedValue = null;
		}

		if (metadata.getEnumClass() != null && returnedValue != null) {
			if (metadata.isMultivalue()) {
				returnedValue = (T) EnumWithSmallCodeUtils.toEnumList(metadata.getEnumClass(), (List<String>) returnedValue);
			} else {
				returnedValue = (T) EnumWithSmallCodeUtils.toEnum(metadata.getEnumClass(), (String) returnedValue);
			}
		}

		if (metadata.isMultivalue()) {
			if (returnedValue == null) {
				returnedValue = (T) Collections.unmodifiableList(Collections.emptyList());
			} else {
				returnedValue = (T) Collections.unmodifiableList((List<? extends T>) returnedValue);
			}
		}

		return returnedValue;
	}

	private Object getConvertedValue(Object rawValue, Metadata metadata) {
		if (!isConvertedValue(metadata)) {
			return rawValue;
		}

		if (rawValue == null) {
			return null;
		}

		if (structuredValues == null) {
			structuredValues = new HashMap<>();
		}

		Object convertedValue = structuredValues.get(metadata.getDataStoreCode());
		if (convertedValue == null) {
			convertedValue = convertToStructuredValue(rawValue, metadata);
			structuredValues.put(metadata.getDataStoreCode(), convertedValue);
		}
		return convertedValue;
	}

	private Object convertToStructuredValue(Object rawValue, Metadata metadata) {
		if (rawValue instanceof List) {
			List<Object> convertedValues = new ArrayList<>();
			for (Object value : (List) rawValue) {
				convertedValues.add(convertToStructuredValue(value, metadata));
			}
			return convertedValues;
		} else {
			return metadata.getStructureFactory().build((String) rawValue);
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
		return metadata.getStructureFactory() != null;
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

		this.version = version;
		this.recordDTO = recordDTO;
		this.modifiedValues.clear();
		if (structuredValues != null) {
			this.structuredValues.clear();
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
	public String getSchemaCode() {
		return schemaCode;
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
				if (modifiableStructure.isDirty()) {
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

		if (recordDTO != null) {
			fields.putAll(recordDTO.getFields());
		} else {
			for (Metadata metadata : schema.getMetadatas()) {
				fields.put(metadata.getDataStoreCode(), null);
			}
		}

		for (Map.Entry<String, Object> entry : modifiedValues.entrySet()) {
			String metadataAtomicCode = new SchemaUtils().getLocalCodeFromDataStoreCode(entry.getKey());
			Metadata metadata = schema.getMetadata(metadataAtomicCode);
			Object value = entry.getValue();

			if (metadata.getStructureFactory() != null) {
				fields.put(entry.getKey(), convertStructuredValueToString(value, metadata));
			} else {
				fields.put(entry.getKey(), value);
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

		return new RecordDTO(id, version, null, fields, copyfields);

	}

	@Override
	public MetadataList getModifiedMetadatas(MetadataSchemaTypes schemaTypes) {
		MetadataList modifiedMetadatas = new MetadataList();

		for (String modifiedMetadataDataStoreCode : getModifiedValues().keySet()) {
			String metadataCode = schemaCode + "_" + SchemaUtils.underscoreSplitWithCache(modifiedMetadataDataStoreCode)[0];
			modifiedMetadatas.add(schemaTypes.getMetadata(metadataCode));
		}

		return modifiedMetadatas.unModifiable();
	}

	//	@Override
	//	public MetadataList getMetadatasWithValue(MetadataSchemaTypes schemaTypes) {
	//		MetadataList modifiedMetadatas = new MetadataList();
	//
	//		Set<String> codes = new HashSet<>(getModifiedValues().keySet());
	//
	//		if (recordDTO != null) {
	//			codes.addAll(recordDTO.getFields().keySet());
	//			codes.addAll(recordDTO.getCopyFields().keySet());
	//		}
	//
	//		for (String modifiedMetadataDataStoreCode : codes) {
	//			if (!"_version_".equals(modifiedMetadataDataStoreCode) && !"collection_s".equals(modifiedMetadataDataStoreCode)) {
	//				String metadataCode = schemaCode + "_" + SchemaUtils.underscoreSplitWithCache(modifiedMetadataDataStoreCode)[0];
	//				if (schemaTypes.hasMetadata(metadataCode)) {
	//					modifiedMetadatas.add(schemaTypes.getMetadata(metadataCode));
	//				}
	//			}
	//		}
	//
	//		return modifiedMetadatas.unModifiable();
	//	}

	public RecordDeltaDTO toRecordDeltaDTO(MetadataSchema schema, List<FieldsPopulator> copyfieldsPopulators) {

		Map<String, Object> modifiedValues = getModifiedValues();
		Map<String, Object> convertedValues = new HashMap<>(modifiedValues);
		Map<String, Object> copyfields = new HashMap<>();

		for (FieldsPopulator populator : copyfieldsPopulators) {
			for (Map.Entry<String, Object> entry : populator.populateCopyfields(schema, this).entrySet()) {
				if (entry.getValue() == null) {
					throw new RecordImplException_PopulatorReturnedNullValue(populator, entry.getKey());
				}
				copyfields.put(entry.getKey(), entry.getValue());
			}
		}

		for (Map.Entry<String, Object> entry : modifiedValues.entrySet()) {
			String localCode = new SchemaUtils().getLocalCodeFromDataStoreCode(entry.getKey());
			Metadata metadata = schema.getMetadata(localCode);
			Object value = entry.getValue();

			if (metadata.getStructureFactory() != null) {
				convertedValues.put(entry.getKey(), convertStructuredValueToString(value, metadata));
			}
		}

		return new RecordDeltaDTO(id, version, convertedValues, recordDTO.getFields(), copyfields);
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "recordDTO");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "recordDTO");
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

		RecordDTO otherVersionRecordDTO = otherVersion.recordDTO;

		List<String> removedKeys = new ArrayList<>();
		for (Entry<String, Object> entry : modifiedValues.entrySet()) {
			String key = entry.getKey();
			boolean specialField = key.equals("schema_s") || key.equals("id") || key.equals("_version_")
					|| key.equals("autocomplete_ss") || key.equals("collection_s")
					|| key.equals("modifiedOn_dt") || key.equals("createdOn_dt") || key.equals("modifiedById_s");

			if (!specialField) {
				String metadataCode = new SchemaUtils().getLocalCodeFromDataStoreCode(key);
				if (!specialField && schema.getMetadata(metadataCode).getDataEntry().getType() == DataEntryType.MANUAL) {
					Object initialValue = recordDTO.getFields().get(entry.getKey());
					Object modifiedValue = entry.getValue();
					Object currentValue = otherVersionRecordDTO.getFields().get(entry.getKey());
					if (LangUtils.areNullableEqual(currentValue, modifiedValue)) {
						//Both transactions made the same change on that field
						removedKeys.add(entry.getKey());
					} else {

						if (!LangUtils.areNullableEqual(currentValue, initialValue)) {
							throw new RecordRuntimeException.CannotMerge();
						}
					}
				}
			}
		}
		for (String removedKey : removedKeys) {
			modifiedValues.remove(removedKey);
		}

		//		for (Entry<String, Object> entry : otherVersionRecordDTO.getFields().entrySet()) {
		//			String key = entry.getKey();
		//			if (!isCreationOrModificationInfo(key)) {
		//				Object dataStoreValue = correctValue(otherVersionRecordDTO.getFields().get(key));
		//				Object value = correctValue(recordDTO.getFields().get(key));
		//
		//				String metadataCode = new SchemaUtils().getLocalCodeFromDataStoreCode(key);
		//
		//				boolean specialField =
		//						key.equals("schema_s") || key.equals("id") || key.equals("_version_") || key.equals("autocomplete_ss")
		//								|| key.equals("collection_s");
		//
		//				if (!specialField && schema.getMetadata(metadataCode).getDataEntry().getType() != DataEntryType.CALCULATED) {
		//					if (!LangUtils.areNullableEqual(dataStoreValue, value)) {
		//
		//						if (modifiedValues.containsKey(key) && !modifiedValues.get(key).equals(dataStoreValue)) {
		//							throw new RecordRuntimeException.CannotMerge();
		//						} else {
		//							toMerge.put(entry.getKey(), dataStoreValue);
		//						}
		//					}
		//				}
		//			}
		//		}
		this.version = otherVersion.getVersion();
		this.recordDTO = otherVersion.getRecordDTO();
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
		if (!isSaved()) {
			RecordDTO dto = toNewDocumentDTO(schema, new ArrayList<FieldsPopulator>()).withVersion(
					version);
			refresh(version, dto);
		} else {
			RecordDTO dto = recordDTO.createCopyWithDelta(toRecordDeltaDTO(schema, new ArrayList<FieldsPopulator>()))
					.withVersion(version);
			refresh(version, dto);
		}

	}

	@Override
	public String getCollection() {
		return collection;
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
		return Boolean.TRUE != get(Schemas.LOGICALLY_DELETED_STATUS);
	}

	@Override
	public List<String> getFollowers() {
		if (modifiedValues.containsKey("followers_ss")) {
			followers = (List<String>) modifiedValues.get("followers_ss");
		}
		return followers;
	}

	@Override
	public Record getCopyOfOriginalRecord() {
		if (recordDTO == null) {
			throw new RecordImplException_UnsupportedOperationOnUnsavedRecord("getCopyOfOriginalRecord", id);
		}
		return new RecordImpl(recordDTO);
	}

	@Override
	public String getIdTitle() {
		String title = get(Schemas.TITLE);
		return id + (title == null ? "" : (":" + get(Schemas.TITLE)));
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
		modifiedValues.put(metadata.getDataStoreCode(), get(metadata));
	}

}
