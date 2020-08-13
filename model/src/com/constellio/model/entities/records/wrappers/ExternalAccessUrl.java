package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDate;

public class ExternalAccessUrl extends RecordWrapper {
	public static final String SCHEMA_TYPE = "externalAccessUrl";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String TOKEN = "token";
	public static final String FULLNAME = "fullname";
	public static final String EMAIL = "email";
	public static final String EXPIRATION_DATE = "expirationDate";
	public static final String STATUS = "status";
	public static final String ACCESS_RECORD = "accessRecord";

	public ExternalAccessUrl(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getToken() {
		return get(TOKEN);
	}

	public ExternalAccessUrl setToken(String token) {
		set(TOKEN, token);
		return this;
	}

	public String getFullname() {
		return get(FULLNAME);
	}

	public ExternalAccessUrl setFullname(String name) {
		set(FULLNAME, name);
		return this;
	}

	public String getEmail() {
		return get(EMAIL);
	}

	public ExternalAccessUrl setEmail(String email) {
		set(EMAIL, email);
		return this;
	}

	public LocalDate getExpirationDate() {
		return get(EXPIRATION_DATE);
	}

	public ExternalAccessUrl setExpirationDate(LocalDate date) {
		set(EXPIRATION_DATE, date);
		return this;
	}

	public ExternalAccessUrlStatus getStatus() {
		return get(STATUS);
	}

	public ExternalAccessUrl setStatus(ExternalAccessUrlStatus status) {
		set(STATUS, status);
		return this;
	}

	public String getAccessRecord() {
		return get(ACCESS_RECORD);
	}

	public ExternalAccessUrl setAccessRecord(String recordId) {
		set(ACCESS_RECORD, recordId);
		return this;
	}
}
