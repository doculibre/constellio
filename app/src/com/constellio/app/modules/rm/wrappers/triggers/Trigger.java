package com.constellio.app.modules.rm.wrappers.triggers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

//Déclancheur
public class Trigger extends RecordWrapper {


	public static final String SCHEMA_TYPE = "trigger";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public Trigger(Record record,
				   MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	//Type de déclancheur (ref simplevaleur TriggerType)
	public static final String TYPE = "type";

	//Critères (ref multivaleur structure Criterion déjà existante)
	public static final String CRITERIA = "criteria";

	//Actions (ref multivaleur TriggerAction)
	public static final String ACTIONS = "actions";

}
