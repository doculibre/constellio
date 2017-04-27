package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;

public class Collection extends RecordWrapper {

	public static final String SCHEMA_TYPE = "collection";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String SYSTEM_COLLECTION = "_system_";

	public static final String CODE = "code";

	public static final String NAME = "name";

	public static final String LANGUAGES = "languages";

	public static final String CONSERVATION_CALENDAR_NUMBER = "conservationCalendarNumber";

	public static final String ORGANIZATION_NUMBER = "organizationNumber";

	public Collection(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getCode() {
		return get(CODE);
	}

	public String getName() {
		return get(NAME);
	}

	public Collection setName(String name) {
		set(NAME, name);
		return this;
	}

	public String getConservationCalendarNumber() {
		return get(CONSERVATION_CALENDAR_NUMBER);
	}

	public Collection setConservationCalendarNumber(String conservationCalendarNumber) {
		set(CONSERVATION_CALENDAR_NUMBER, conservationCalendarNumber);
		return this;
	}

	public String getOrganizationNumber() {
		return get(ORGANIZATION_NUMBER);
	}

	public Collection setOrganizationNumber(String organizationNumber) {
		set(ORGANIZATION_NUMBER, organizationNumber);
		return this;
	}

	public List<String> getLanguages() {
		return get(LANGUAGES);
	}

}
