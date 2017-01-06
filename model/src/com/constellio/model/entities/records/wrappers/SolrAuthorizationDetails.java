package com.constellio.model.entities.records.wrappers;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.global.AuthorizationDetails;

import org.joda.time.LocalDate;

import java.util.List;

/**
 * Created by Constellio on 2016-12-21.
 */
public class SolrAuthorizationDetails extends RecordWrapper implements AuthorizationDetails {
	public static final String SCHEMA_TYPE = "authorizationDetails";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String IDENTIFIER = "identifier";
	public static final String ROLES = "roles";
	public static final String START_DATE = "startDate";
	public static final String END_DATE = "endDate";
	public static final String SYNCED = "synced";

	public SolrAuthorizationDetails(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public SolrAuthorizationDetails setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public String getIdentifier() {
		return get(IDENTIFIER);
	}

	public SolrAuthorizationDetails setIdentifier(String identifier) {
		set(IDENTIFIER, identifier);
		return this;
	}

	@Override
	public List<String> getRoles() {
		return get(ROLES);
	}

	public SolrAuthorizationDetails setRoles(List<String> roles) {
		set(ROLES, roles);
		return this;
	}

	@Override
	public LocalDate getStartDate() {
		return get(START_DATE);
	}

	public SolrAuthorizationDetails setStartDate(LocalDate startDate) {
		set(START_DATE, startDate);
		return this;
	}

	@Override
	public LocalDate getEndDate() {
		return get(END_DATE);
	}

	public SolrAuthorizationDetails setEndDate(LocalDate endDate) {
		set(END_DATE, endDate);
		return this;
	}

	@Override
	public boolean isSynced() {
		return Boolean.TRUE.equals(get(SYNCED));
	}

	@Override
	public AuthorizationDetails withNewEndDate(LocalDate endate) {
		setEndDate(endate);
		return this;
	}

	@Override
	public boolean isFutureAuthorization() {
		return getStartDate() != null && TimeProvider.getLocalDate().isBefore(getStartDate());
	}

	public SolrAuthorizationDetails setSynced(Boolean isSynced) {
		set(SYNCED, isSynced);
		return this;
	}
}
