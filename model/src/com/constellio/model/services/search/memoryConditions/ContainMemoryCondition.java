package com.constellio.model.services.search.memoryConditions;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;

public class ContainMemoryCondition implements InMemoryCondition {
    MetadataSchemaTypes types;

    DataStoreField field;

    Object value;
    List<Object> liste;

    public ContainMemoryCondition (List<Object> liste, DataStoreField field, Object value) {
        this.field = field;
        this.value = value;
        this.liste=liste;
    }

    @Override
    public boolean isReturnable(Record record) {
        boolean result = false;
        //MetadataSchema schema = types.getSchema(record.getSchemaCode());
        Metadata metadata = (Metadata) field;

        Object recordValue = record.get(metadata);


        return liste.contains(recordValue);
    }
}
