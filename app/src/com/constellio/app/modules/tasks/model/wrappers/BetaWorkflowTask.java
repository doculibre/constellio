package com.constellio.app.modules.tasks.model.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class BetaWorkflowTask extends Task {
	public BetaWorkflowTask(Record record,
			MetadataSchemaTypes types) {
		super(record, types);
	}
}
