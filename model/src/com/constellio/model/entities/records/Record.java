package com.constellio.model.entities.records;

import java.io.Serializable;
import java.util.List;

import com.constellio.model.entities.CollectionObject;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataList;

public interface Record extends Serializable, CollectionObject {

	public static final String PUBLIC_TOKEN = "__public__";

	public String getId();

	String getTitle();

	public long getVersion();

	public long getDataMigrationVersion();

	public String getSchemaCode();

	String getTypeCode();

	public boolean isDirty();

	public boolean isFullyLoaded();

	boolean isModified(Metadata metadata);

	Record set(Metadata metadata, Object value);

	<T> T get(Metadata metadata);

	<T> T getNonNullValueIn(List<Metadata> metadatas);

	<T> List<T> getList(Metadata metadata);

	<T> List<T> getValues(Metadata metadata);

	MetadataList getModifiedMetadatas(MetadataSchemaTypes schemaTypes);

	//MetadataList getMetadatasWithValue(MetadataSchemaTypes schemaTypes);

	boolean isSaved();

	String getCollection();

	String getParentId();

	boolean isActive();

	boolean isDisconnected();

	Record getCopyOfOriginalRecord();

	Record getUnmodifiableCopyOfOriginalRecord();

	Record getCopyOfOriginalRecordKeepingOnly(List<Metadata> metadatas);

	String getIdTitle();

	void removeAllFieldsStartingWith(String field);

	void markAsModified(Metadata metadata);

	boolean changeSchema(MetadataSchema wasSchema, MetadataSchema newSchema);

	<T> void addValueToList(Metadata metadata, T value);

	<T> void removeValueFromList(Metadata metadata, T value);

	boolean isOfSchemaType(String type);

}
