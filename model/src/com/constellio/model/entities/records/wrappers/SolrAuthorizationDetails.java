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
	public static final String PRINCIPALS = "principals";
	public static final String START_DATE = "startDate";
	public static final String END_DATE = "endDate";
	public static final String TARGET = "target";
	public static final String TARGET_SCHEMA_TYPE = "targetSchemaType";
	public static final String LAST_TOKEN_RECALCULATE = "lastTokenRecalculate";
	public static final String OVERRIDE_INHERITED = "overrideInherited";
	public static final String SYNCED = "synced";
	public static final String NEGATIVE = "negative";

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
	public List<String> getPrincipals() {
		return get(PRINCIPALS);
	}

	public SolrAuthorizationDetails setPrincipals(List<String> principals) {
		set(PRINCIPALS, principals);
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

	public LocalDate getLastTokenRecalculate() {
		return get(LAST_TOKEN_RECALCULATE);
	}

	public SolrAuthorizationDetails setLastTokenRecalculate(LocalDate lastTokenRecalculate) {
		set(LAST_TOKEN_RECALCULATE, lastTokenRecalculate);
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

	public String getTargetSchemaType() {
		return get(TARGET_SCHEMA_TYPE);
	}

	public SolrAuthorizationDetails setTargetSchemaType(String targetSchemaType) {
		set(TARGET_SCHEMA_TYPE, targetSchemaType);
		return this;
	}

	public boolean isOverrideInherited() {
		return getBooleanWithDefaultValue(OVERRIDE_INHERITED, false);
	}

	public SolrAuthorizationDetails setOverrideInherited(boolean overrideInherited) {
		set(OVERRIDE_INHERITED, overrideInherited ? true : null);
		return this;
	}

	public String getTarget() {
		return get(TARGET);
	}

	public SolrAuthorizationDetails setTarget(String target) {
		set(TARGET, target);
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

	@Override
	public boolean isNegative() {
		return getBooleanWithDefaultValue(NEGATIVE, false);
	}

	public SolrAuthorizationDetails setNegative(boolean negative) {
		set(NEGATIVE, Boolean.TRUE.equals(negative) ? true : null);
		return this;
	}

	@Override
	public boolean isActiveAuthorization() {
		return isActiveAuthorizationAtDate(TimeProvider.getLocalDate());
	}

	private boolean isActiveAuthorizationAtDate(LocalDate date) {
		LocalDate startDate = getStartDate();
		LocalDate endDate = getEndDate();
		if (startDate != null && endDate == null) {
			return !startDate.isAfter(date);

		} else if (startDate == null && endDate != null) {
			return !endDate.isBefore(date);

		} else if (startDate != null && endDate != null) {
			return !startDate.isAfter(date) && !endDate.isBefore(date);

		} else {
			return true;
		}
	}

	public boolean hasModifiedStatusSinceLastTokenRecalculate() {
		if (getStartDate() != null || getEndDate() != null) {

			if (getLastTokenRecalculate() == null) {
				return true;

			} else {

				boolean wasActiveDuringLastRecalculate = isActiveAuthorizationAtDate(getLastTokenRecalculate());
				boolean isCurrentlyActive = isActiveAuthorization();

				return wasActiveDuringLastRecalculate != isCurrentlyActive;

			}

		} else {
			return false;
		}
	}

	public static SolrAuthorizationDetails wrapNullable(Record record, MetadataSchemaTypes types) {
		return record == null ? null : new SolrAuthorizationDetails(record, types);
	}

	public SolrAuthorizationDetails getCopyOfOriginalRecord() {
		return SolrAuthorizationDetails.wrapNullable(wrappedRecord.getCopyOfOriginalRecord(), types);
	}

	public SolrAuthorizationDetails getUnmodifiableCopyOfOriginalRecord() {
		return SolrAuthorizationDetails.wrapNullable(wrappedRecord.getUnmodifiableCopyOfOriginalRecord(), types);
	}
}
