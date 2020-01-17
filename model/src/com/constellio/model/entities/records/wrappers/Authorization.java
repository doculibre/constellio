package com.constellio.model.entities.records.wrappers;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordId;
import org.joda.time.LocalDate;

import java.util.List;

public class Authorization extends RecordWrapper {
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

	public Authorization(Record record,
						 MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public Authorization setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public String getIdentifier() {
		return get(IDENTIFIER);
	}

	public Authorization setIdentifier(String identifier) {
		set(IDENTIFIER, identifier);
		return this;
	}

	public List<String> getPrincipals() {
		return get(PRINCIPALS);
	}

	public Authorization setPrincipals(List<String> principals) {
		set(PRINCIPALS, principals);
		return this;
	}


	public List<String> getRoles() {
		return get(ROLES);
	}

	public Authorization setRoles(List<String> roles) {
		set(ROLES, roles);
		return this;
	}

	public LocalDate getStartDate() {
		return get(START_DATE);
	}

	public Authorization setStartDate(LocalDate startDate) {
		set(START_DATE, startDate);
		return this;
	}

	public LocalDate getLastTokenRecalculate() {
		return get(LAST_TOKEN_RECALCULATE);
	}

	public Authorization setLastTokenRecalculate(LocalDate lastTokenRecalculate) {
		set(LAST_TOKEN_RECALCULATE, lastTokenRecalculate);
		return this;
	}

	public LocalDate getEndDate() {
		return get(END_DATE);
	}

	public Authorization setEndDate(LocalDate endDate) {
		set(END_DATE, endDate);
		return this;
	}

	public String getTargetSchemaType() {
		return get(TARGET_SCHEMA_TYPE);
	}

	public Authorization setTargetSchemaType(String targetSchemaType) {
		set(TARGET_SCHEMA_TYPE, targetSchemaType);
		return this;
	}

	public boolean isOverrideInherited() {
		return getBooleanWithDefaultValue(OVERRIDE_INHERITED, false);
	}

	public Authorization setOverrideInherited(boolean overrideInherited) {
		set(OVERRIDE_INHERITED, overrideInherited ? true : null);
		return this;
	}

	public String getTarget() {
		return get(TARGET);
	}

	public RecordId getTargetRecordId() {
		String stringId = get(TARGET);
		return stringId == null ? null : RecordId.toId(stringId);
	}

	public int getTargetRecordIntId() {
		String stringId = get(TARGET);
		return stringId == null ? 0 : RecordId.toIntId(stringId);
	}

	public Authorization setTarget(String target) {
		set(TARGET, target);
		return this;
	}

	public boolean isSynced() {
		return Boolean.TRUE.equals(get(SYNCED));
	}

	public Authorization withNewEndDate(LocalDate endate) {
		setEndDate(endate);
		return this;
	}

	public boolean isFutureAuthorization() {
		return getStartDate() != null && TimeProvider.getLocalDate().isBefore(getStartDate());
	}

	public Authorization setSynced(Boolean isSynced) {
		set(SYNCED, isSynced);
		return this;
	}

	public boolean isNegative() {
		return getBooleanWithDefaultValue(NEGATIVE, false);
	}

	public Authorization setNegative(boolean negative) {
		set(NEGATIVE, Boolean.TRUE.equals(negative) ? true : null);
		return this;
	}

	public boolean isActiveAuthorization() {
		return isActiveAuthorizationAtDate(TimeProvider.getLocalDate());
	}

	public String toString() {
		return "Giving " + (isNegative() ? "negative " : "") + getRoles() + " to " + getPrincipals() + " on " + getTarget() + " (" + getTargetSchemaType() + ")";
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

	public static Authorization wrapNullable(Record record, MetadataSchemaTypes types) {
		return record == null ? null : new Authorization(record, types);
	}

	public Authorization getCopyOfOriginalRecord() {
		return Authorization.wrapNullable(wrappedRecord.getCopyOfOriginalRecord(), types);
	}

	public Authorization getUnmodifiableCopyOfOriginalRecord() {
		return Authorization.wrapNullable(wrappedRecord.getUnmodifiableCopyOfOriginalRecord(), types);
	}

	//TODO Temporaire
	public static boolean isSecurableSchemaType(String schemaType) {
		return "folder".equals(schemaType) || "document".equals(schemaType);
	}
}
