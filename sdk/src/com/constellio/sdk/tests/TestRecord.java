package com.constellio.sdk.tests;

import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.sdk.tests.setups.SchemaShortcuts;

public class TestRecord extends RecordImpl {

	SchemaShortcuts schemaShortcuts;

	public TestRecord(SchemaShortcuts schema) {
		super(schema.code(), schema.collection(), "TestRecord_" + UUID.randomUUID().toString());
		this.schemaShortcuts = schema;
	}

	public TestRecord(SchemaShortcuts schema, String id) {
		super(schema.code(), schema.collection(), id);
		this.schemaShortcuts = schema;
	}

	public TestRecord(String schema, String collection, String id) {
		super(schema, collection, id);
	}

	public TestRecord(String schema, String collection) {
		super(schema, collection, "TestRecord_" + UUID.randomUUID().toString());
	}

	public TestRecord(RecordDTO recordDTO) {
		super(recordDTO);
	}

	public TestRecord(MetadataSchema schema, String id) {
		this(schema.getCode(), schema.getCollection(), id);
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
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}
