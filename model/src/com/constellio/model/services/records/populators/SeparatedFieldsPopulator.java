package com.constellio.model.services.records.populators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.FieldsPopulator;
import com.constellio.model.services.records.RecordImplRuntimeException.RecordImplException_PopulatorReturnedNullValue;

public abstract class SeparatedFieldsPopulator implements FieldsPopulator {

	boolean fullRewrite;
	MetadataSchemaTypes types;

	protected SeparatedFieldsPopulator(
			MetadataSchemaTypes types, boolean fullRewrite) {
		this.types = types;
		this.fullRewrite = fullRewrite;
	}

	@Override
	public Map<String, Object> populateCopyfields(MetadataSchema schema, Record record) {
		Map<String, Object> fields = new HashMap<>();

		List<Metadata> metadatas = record.getModifiedMetadatas(types);
		if (fullRewrite) {
			metadatas = schema.getMetadatas();
		}

		for (Metadata modifiedMetadata : metadatas) {
			Object value = record.get(modifiedMetadata);

			populateCopyFields(fields, modifiedMetadata, value);
		}

		return fields;
	}

	private void populateCopyFields(Map<String, Object> fields, Metadata modifiedMetadata, Object value) {
		Map<String, Object> values = populateCopyfields(modifiedMetadata, value);
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			if (entry.getValue() == null) {
				throw new RecordImplException_PopulatorReturnedNullValue(this, entry.getKey());
			}
			fields.put(entry.getKey(), entry.getValue());
		}
	}

	abstract Map<String, Object> populateCopyfields(Metadata metadata, Object value);
}
