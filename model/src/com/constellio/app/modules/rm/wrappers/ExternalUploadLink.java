package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ExternalAccessUrl;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ExternalUploadLink extends ExternalAccessUrl {
	public static final String SCHEMA = "upload";
	public static final String FULL_SCHEMA = SCHEMA_TYPE + "_" + SCHEMA;

	public ExternalUploadLink(Record record, MetadataSchemaTypes types) {
		super(record, types);
	}
}
