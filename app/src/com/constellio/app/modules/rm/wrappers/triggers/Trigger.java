package com.constellio.app.modules.rm.wrappers.triggers;

import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;

//Déclancheur
public class Trigger extends RecordWrapper {
	public static final String SCHEMA_TYPE = "trigger";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String DESCRIPTION = "description";

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

	public static final String TARGET = "target";

	public String getType() {
		return get(TYPE);
	}


	public Trigger setType(String type) {
		set(TYPE, type);
		return this;
	}

	public List<Criterion> getCriteria() {
		return getList(CRITERIA);
	}

	public Trigger setCriteria(List<Criterion> criteria) {
		return setCriteria(criteria);
	}

	public List<String> getActions() {
		return getList(ACTIONS);
	}

	public Trigger setActions(List<String> actions) {
		set(ACTIONS, actions);
		return this;
	}

	public List<String> getTarget() {
		return getList(TARGET);
	}

	public Trigger setTarget(List<String> trigger) {
		set(TARGET, trigger);
		return this;
	}
}
