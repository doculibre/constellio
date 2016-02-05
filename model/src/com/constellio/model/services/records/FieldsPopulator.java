package com.constellio.model.services.records;

import java.util.Map;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;

public interface FieldsPopulator {

	Map<String, Object> populateCopyfields(MetadataSchema schema, Record record);

}
