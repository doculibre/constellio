package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class RMCollection extends Collection {

	public RMCollection(Record record,
			MetadataSchemaTypes types) {
		super(record, types);
	}

}
