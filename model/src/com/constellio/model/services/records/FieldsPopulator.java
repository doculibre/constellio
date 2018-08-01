package com.constellio.model.services.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;

import java.util.Map;

public interface FieldsPopulator {

	Map<String, Object> populateCopyfields(MetadataSchema schema, Record record);

}
