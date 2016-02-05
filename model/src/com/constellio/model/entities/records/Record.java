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

	public long getVersion();

	public String getSchemaCode();

	public boolean isDirty();

	public boolean isFullyLoaded();

	boolean isModified(Metadata metadata);

	Record set(Metadata metadata, Object value);

	<T> T get(Metadata metadata);

	<T> T getNonNullValueIn(List<Metadata> metadatas);

	<T> List<T> getList(Metadata metadata);

	MetadataList getModifiedMetadatas(MetadataSchemaTypes schemaTypes);

	//MetadataList getMetadatasWithValue(MetadataSchemaTypes schemaTypes);

	boolean isSaved();

	String getCollection();

	String getParentId();

	boolean isActive();

	List<String> getFollowers();

	Record getCopyOfOriginalRecord();

	String getIdTitle();

	void removeAllFieldsStartingWith(String field);

	void markAsModified(Metadata metadata);

	void changeSchema(MetadataSchema wasSchema, MetadataSchema newSchema);
}
