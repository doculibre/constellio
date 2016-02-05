package com.constellio.model.services.schemas.impacts;

import com.constellio.model.entities.batchprocess.BatchProcessAction;

public class SchemaTypesAlterationImpact {

	BatchProcessAction action;

	String schemaType;

	public SchemaTypesAlterationImpact(BatchProcessAction action, String schemaType) {
		this.action = action;
		this.schemaType = schemaType;
	}

	public BatchProcessAction getAction() {
		return action;
	}

	public String getSchemaType() {
		return schemaType;
	}

}
