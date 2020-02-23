package com.constellio.model.entities.records;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.CollectionObject;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataList;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public interface Record extends Serializable, CollectionObject, Supplier<Record> {

	String PUBLIC_TOKEN = "__public__";

	String getId();

	short getTypeId();

	RecordId getRecordId();

	String getTitle();

	long getVersion();

	long getDataMigrationVersion();

	String getSchemaCode();

	String getTypeCode();

	boolean isDirty();

	@Deprecated
	default boolean isFullyLoaded() {
		return getLoadedFieldsMode() == RecordDTOMode.FULLY_LOADED;
	}

	@Deprecated
	default boolean isSummary() {
		return getLoadedFieldsMode() == RecordDTOMode.SUMMARY;
	}

	RecordDTOMode getLoadedFieldsMode();

	boolean isModified(Metadata metadata);

	default boolean isAnyModified(Metadata... metadatas) {
		for (Metadata metadata : metadatas) {
			if (isModified(metadata)) {
				return true;
			}
		}
		return false;
	}

	default boolean isAnyModified(List<Metadata> metadatas) {
		for (Metadata metadata : metadatas) {
			if (isModified(metadata)) {
				return true;
			}
		}
		return false;
	}

	Record set(Metadata metadata, Object value);

	Record set(Metadata metadata, Locale locale, Object value);

	<T> T get(Metadata metadata, GetMetadataOption... option);

	<T> T get(Metadata metadata, Locale locale, LocalisedRecordMetadataRetrieval mode, GetMetadataOption... option);

	<T> T get(Metadata metadata, Locale locale, GetMetadataOption... option);

	<T> T getNonNullValueIn(List<Metadata> metadatas, GetMetadataOption... option);

	<T> List<T> getList(Metadata metadata, GetMetadataOption... option);

	<T> List<T> getList(Metadata metadata, Locale locale, LocalisedRecordMetadataRetrieval mode,
						GetMetadataOption... option);

	<T> List<T> getValues(Metadata metadata, GetMetadataOption... option);

	<T> List<T> getValues(Metadata metadata, Locale locale, LocalisedRecordMetadataRetrieval mode,
						  GetMetadataOption... option);

	List<Metadata> getModifiedMetadataList(MetadataSchemaTypes schemaTypes);

	@Deprecated
	/**
	 * Slow, use getModifiedMetadataList instead
	 */
	MetadataList getModifiedMetadatas(MetadataSchemaTypes schemaTypes);

	//MetadataList getMetadatasWithValue(MetadataSchemaTypes schemaTypes);

	boolean isSaved();

	String getCollection();

	CollectionInfo getCollectionInfo();

	String getParentId();

	boolean isActive();

	boolean isDisconnected();

	Record getCopyOfOriginalRecord();

	Record getUnmodifiableCopyOfOriginalRecord();

	Record getCopyOfOriginalRecordKeepingOnly(List<Metadata> metadatas);

	String getIdTitle();

	String getSchemaIdTitle();

	void removeAllFieldsStartingWith(String field);

	void markAsModified(Metadata metadata);

	boolean changeSchema(MetadataSchema wasSchema, MetadataSchema newSchema);

	<T> void addValueToList(Metadata metadata, T value);

	<T> void removeValueFromList(Metadata metadata, T value);

	boolean isOfSchemaType(String type);

	void markAsSaved(long version, MetadataSchema schema);

	RecordDTOMode getRecordDTOMode();

	default boolean isLogicallyDeleted() {
		return Boolean.TRUE.equals(get(Schemas.LOGICALLY_DELETED_STATUS));
	}

	RecordDTO getRecordDTO();

	enum GetMetadataOption {
		NO_SUMMARY_METADATA_VALIDATION, DIRECT_GET_FROM_DTO, RARELY_HAS_VALUE;
	}

}
