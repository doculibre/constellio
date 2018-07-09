package com.constellio.model.services.search.memoryConditions;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class EqualMemoryCondition implements InMemoryCondition {

	MetadataSchemaTypes types;

	DataStoreField field;

	Object value;

	public EqualMemoryCondition(MetadataSchemaTypes types, DataStoreField field, Object value) {
		this.field = field;
		this.value = value;
	}

	@Override
	public boolean isReturnable(Record record) {

		//MetadataSchema schema = types.getSchema(record.getSchemaCode());
		Metadata metadata = (Metadata) field;

		Object recordValue = record.get(metadata);

		return LangUtils.isEqual(recordValue, value);
	}
}
