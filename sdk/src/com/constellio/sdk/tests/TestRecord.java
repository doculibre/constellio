package com.constellio.sdk.tests;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.sdk.tests.setups.SchemaShortcuts;
import org.mockito.Mockito;

import java.util.UUID;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;

public class TestRecord extends RecordImpl {

	SchemaShortcuts schemaShortcuts;

	public TestRecord(MetadataSchema schema) {
		super(schema, "TestRecord_" + UUID.randomUUID().toString());
	}

	public TestRecord(MetadataSchema schema, String id) {
		super(schema, id);
	}

	public TestRecord(SchemaShortcuts schema) {
		super(schema.instance(), "TestRecord_" + UUID.randomUUID().toString());
		this.schemaShortcuts = schema;
	}

	public TestRecord(SchemaShortcuts schema, String id) {
		super(schema.instance(), id);
		this.schemaShortcuts = schema;
	}

	@Deprecated
	public TestRecord(String schema, String collection, String id) {
		super(mockSchema(schema, collection), id);
	}

	@Deprecated
	public TestRecord(String schemaCode, String collection) {
		super(mockSchema(schemaCode, collection), "TestRecord_" + UUID.randomUUID().toString());
	}

	private static MetadataSchema mockSchema(String schemaCode, String collection) {
		MetadataSchema schema = Mockito.mock(MetadataSchema.class);
		when(schema.getCode()).thenReturn(schemaCode);
		when(schema.getCollection()).thenReturn(collection);
		when(schema.getCollectionInfo()).thenReturn(new CollectionInfo((byte) 0, collection, "fr", asList("fr")));
		return schema;
	}

	public TestRecord(RecordDTO recordDTO, CollectionInfo collectionInfo) {
		super(recordDTO, collectionInfo);
	}

	public void markAsModified(Metadata metadata) {
		modifiedValues.put(metadata.getDataStoreCode(), get(metadata));
	}

	public TestRecord withTitle(String title) {
		set(Schemas.TITLE, title);
		return this;
	}

	public TestRecord set(String localCode, Object value) {
		if (schemaShortcuts == null) {
			throw new IllegalStateException("Must use constructor with SchemaShortcuts to use this method");
		}

		set(schemaShortcuts.instance().get(localCode), value);
		return this;
	}

	@Override
	public TestRecord set(Metadata metadata, Object value) {
		super.set(metadata, value);
		return this;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}
