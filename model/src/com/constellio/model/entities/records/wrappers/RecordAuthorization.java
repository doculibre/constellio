package com.constellio.model.entities.records.wrappers;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDate;

import java.util.List;

public class RecordAuthorization extends RecordWrapper implements Authorization {
	public static final String SCHEMA_TYPE = "authorizationDetails";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String IDENTIFIER = "identifier";
	public static final String ROLES = "roles";
	public static final String PRINCIPALS = "principals";
	public static final String SHARED_BY = "sharedBy";
	public static final String START_DATE = "startDate";
	public static final String END_DATE = "endDate";
	public static final String TARGET = "target";
	public static final String TARGET_SCHEMA_TYPE = "targetSchemaType";
	public static final String LAST_TOKEN_RECALCULATE = "lastTokenRecalculate";
	public static final String OVERRIDE_INHERITED = "overrideInherited";
	public static final String SYNCED = "synced";
	public static final String NEGATIVE = "negative";

	public RecordAuthorization(Record record,
							   MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public RecordAuthorization setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public String getIdentifier() {
		return get(IDENTIFIER);
	}

	public RecordAuthorization setIdentifier(String identifier) {
		set(IDENTIFIER, identifier);
		return this;
	}

	@Override
	public List<String> getPrincipals() {
		return get(PRINCIPALS);
	}

	@Override
	public List<RecordId> getPrincipalsIds() {
		return RecordId.toIds(get(PRINCIPALS));
	}

	public RecordAuthorization setPrincipals(List<String> principals) {
		set(PRINCIPALS, principals);
		return this;
	}

	@Override
	public String getSharedBy() {
		return get(SHARED_BY);
	}

	public RecordAuthorization setSharedBy(String sharedBy) {
		set(SHARED_BY, sharedBy);
		return this;
	}

	@Override
	public List<String> getRoles() {
		return get(ROLES);
	}

	public RecordAuthorization setRoles(List<String> roles) {
		set(ROLES, roles);
		return this;
	}

	@Override
	public LocalDate getStartDate() {
		return get(START_DATE);
	}

	public RecordAuthorization setStartDate(LocalDate startDate) {
		set(START_DATE, startDate);
		return this;
	}

	@Override
	public LocalDate getLastTokenRecalculate() {
		return get(LAST_TOKEN_RECALCULATE);
	}

	public RecordAuthorization setLastTokenRecalculate(LocalDate lastTokenRecalculate) {
		set(LAST_TOKEN_RECALCULATE, lastTokenRecalculate);
		return this;
	}

	@Override
	public LocalDate getEndDate() {
		return get(END_DATE);
	}

	public RecordAuthorization setEndDate(LocalDate endDate) {
		set(END_DATE, endDate);
		return this;
	}

	@Override
	public String getTargetSchemaType() {
		return get(TARGET_SCHEMA_TYPE);
	}

	public RecordAuthorization setTargetSchemaType(String targetSchemaType) {
		set(TARGET_SCHEMA_TYPE, targetSchemaType);
		return this;
	}

	@Override
	public boolean isOverrideInherited() {
		return getBooleanWithDefaultValue(OVERRIDE_INHERITED, false);
	}

	public RecordAuthorization setOverrideInherited(boolean overrideInherited) {
		set(OVERRIDE_INHERITED, overrideInherited ? true : null);
		return this;
	}

	@Override
	public String getTarget() {
		return get(TARGET);
	}

	@Override
	public RecordId getTargetRecordId() {
		String stringId = get(TARGET);
		return stringId == null ? null : RecordId.toId(stringId);
	}

	@Override
	public int getTargetRecordIntId() {
		String stringId = get(TARGET);
		return stringId == null ? 0 : RecordId.toIntId(stringId);
	}

	public RecordAuthorization setTarget(String target) {
		set(TARGET, target);
		return this;
	}

	@Override
	public boolean isSynced() {
		return Boolean.TRUE.equals(get(SYNCED));
	}

	public RecordAuthorization withNewEndDate(LocalDate endate) {
		setEndDate(endate);
		return this;
	}

	@Override
	public boolean isFutureAuthorization() {
		return getStartDate() != null && TimeProvider.getLocalDate().isBefore(getStartDate());
	}

	public RecordAuthorization setSynced(Boolean isSynced) {
		set(SYNCED, isSynced);
		return this;
	}

	public boolean isNegative() {
		if (!wrappedRecord.isDirty() && wrappedRecord.isSaved()) {
			return Boolean.TRUE.equals(wrappedRecord.getRecordDTO().getFields().get("negative_s"));
		}

		return getBooleanWithDefaultValue(NEGATIVE, false);
	}

	public RecordAuthorization setNegative(boolean negative) {
		set(NEGATIVE, Boolean.TRUE.equals(negative) ? true : null);
		return this;
	}

	@Override
	public boolean isActiveAuthorization() {
		return isActiveAuthorizationAtDate(TimeProvider.getLocalDate());
	}

	@Override
	public RecordId getSource() {
		return null;
	}

	@Override
	public boolean isCascading() {
		return false;
	}

	@Override
	public String toString() {
		return getAsString();
	}

	public static RecordAuthorization wrapNullable(Record record, MetadataSchemaTypes types) {
		return record == null ? null : new RecordAuthorization(record, types);
	}

	public RecordAuthorization getCopyOfOriginalRecord() {
		return RecordAuthorization.wrapNullable(wrappedRecord.getCopyOfOriginalRecord(), types);
	}

	public RecordAuthorization getUnmodifiableCopyOfOriginalRecord() {
		return RecordAuthorization.wrapNullable(wrappedRecord.getUnmodifiableCopyOfOriginalRecord(), types);
	}

	//TODO Temporaire
	public static boolean isSecurableSchemaType(String schemaType) {
		return "folder".equals(schemaType) || "document".equals(schemaType);
	}
}
