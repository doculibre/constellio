package com.constellio.model.entities.records.wrappers;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.entities.CollectionObject;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Record.GetMetadataOption;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException.MetadataSchemaTypesMustBeNotNull;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException.RecordWrapperRuntimeException_CannotUseDisconnectedRecordWrapper;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException.WrappedRecordMustBeNotNull;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException.WrappedRecordMustMeetRequirements;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.schemas.SchemaUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class RecordWrapper implements Serializable, CollectionObject, Supplier<Record> {

	public static final String TITLE = Schemas.TITLE_CODE;

	Record wrappedRecord;

	MetadataSchemaTypes types;

	protected Locale locale;

	public RecordWrapper(Record record, MetadataSchemaTypes types, String typeRequirement) {
		this(record, types, typeRequirement, types == null ? null : types.getLanguages().get(0).getLocale());
	}

	public RecordWrapper(Record record, MetadataSchemaTypes types, String typeRequirement, Locale locale) {
		if (record == null) {
			throw new WrappedRecordMustBeNotNull();
		}
		if (types == null) {
			throw new MetadataSchemaTypesMustBeNotNull();
		}
		String schemaCode = record.getSchemaCode();
		if (schemaCode == null || !schemaCode.startsWith(typeRequirement)) {
			throw new WrappedRecordMustMeetRequirements(schemaCode, typeRequirement);
		}
		if (record.getCollection() != null && types.getCollection() != null
			&& !record.getCollection().equals(types.getCollection())) {
			throw new RecordWrapperRuntimeException.WrappedRecordAndTypesCollectionMustBeTheSame(record.getId(),
					record.getCollection(), types.getCollection());
		}

		this.types = types;
		this.wrappedRecord = record;
		this.locale = locale;
	}

	public Record getWrappedRecord() {
		return wrappedRecord;
	}

	public MetadataSchemaTypes getMetadataSchemaTypes() {
		return types;
	}

	public Integer getInteger(String localCode) {
		Number value = get(localCode);
		return value == null ? null : value.intValue();
	}

	public Long getLong(String localCode) {
		Number value = get(localCode);
		return value == null ? null : value.longValue();
	}

	public int getPrimitiveInteger(String localCode) {
		Number value = get(localCode);
		return value == null ? 0 : value.intValue();
	}

	public <T> T get(Metadata metadata, GetMetadataOption... options) {
		return wrappedRecord.get(metadata, options);
	}

	public <T> T get(String localCode, GetMetadataOption... options) {
		ensureConnected();

		if (localCode.contains("_")) {
			localCode = StringUtils.substringAfterLast(localCode, "_");
		}

		Metadata metadata = types.getSchemaOf(wrappedRecord).getMetadata(localCode);
		return wrappedRecord.get(metadata, options);
	}

	public <T> T get(Metadata metadata, Locale locale, GetMetadataOption... options) {
		return wrappedRecord.get(metadata, locale, options);
	}

	public <T> T get(String localCode, Locale locale, GetMetadataOption... options) {
		ensureConnected();

		if (localCode.contains("_")) {
			localCode = StringUtils.substringAfterLast(localCode, "_");
		}

		Metadata metadata = types.getSchemaOf(wrappedRecord).getMetadata(localCode);
		return wrappedRecord.get(metadata, locale, options);
	}

	public <T> T get(Metadata metadata, Locale locale, LocalisedRecordMetadataRetrieval mode,
					 GetMetadataOption... options) {
		return wrappedRecord.get(metadata, locale, mode, options);
	}

	public <T> T get(String localCode, Locale locale, LocalisedRecordMetadataRetrieval mode,
					 GetMetadataOption... options) {
		ensureConnected();

		if (localCode.contains("_")) {
			localCode = StringUtils.substringAfterLast(localCode, "_");
		}

		Metadata metadata = types.getSchemaOf(wrappedRecord).getMetadata(localCode);
		return wrappedRecord.get(metadata, locale, mode, options);
	}

	public <T> T getOriginal(String localCode) {
		Metadata metadata = types.getSchemaOf(wrappedRecord).getMetadata(localCode);
		return wrappedRecord.getCopyOfOriginalRecord().get(metadata);
	}

	public boolean hasValue(String localCode) {
		ensureConnected();
		MetadataSchema schema = types.getSchemaOf(wrappedRecord);

		Metadata metadata = null;
		if (!schema.hasMetadataWithCode(localCode)) {
			return false;
		}
		metadata = schema.get(localCode);

		return metadata.isMultivalue() ? !getList(metadata).isEmpty() : get(metadata) != null;
	}

	public <T> List<T> getList(Metadata metadata) {
		return wrappedRecord.getList(metadata);
	}

	public <T> List<T> getList(String localCode) {
		ensureConnected();
		String code = wrappedRecord.getSchemaCode() + "_" + localCode;
		Metadata metadata = types.getMetadata(code);
		return wrappedRecord.getList(metadata);
	}

	public <T, W extends RecordWrapper> W set(String localCode, T value) {
		ensureConnected();
		String code = wrappedRecord.getSchemaCode() + "_" + localCode;
		Metadata metadata = types.getMetadata(code);
		wrappedRecord.set(metadata, value);
		return (W) this;
	}

	public <T, W extends RecordWrapper> W set(String localeCode, Locale locale, T value) {
		ensureConnected();
		String code = wrappedRecord.getSchemaCode() + "_" + localeCode;
		Metadata metadata = types.getMetadata(code);
		wrappedRecord.set(metadata, locale, value);
		return (W) this;
	}

	public <T, W extends RecordWrapper> W add(String localCode, T... newValues) {
		ensureConnected();
		String code = wrappedRecord.getSchemaCode() + "_" + localCode;
		Metadata metadata = types.getMetadata(code);
		List<Object> values = new ArrayList<>(wrappedRecord.getList(metadata));

		for (T newValue : newValues) {
			if (newValue != null) {
				Object convertedValue;
				if (newValue instanceof Record) {
					convertedValue = ((Record) newValue).getId();

				} else if (newValue instanceof RecordWrapper) {
					convertedValue = ((RecordWrapper) newValue).getId();

				} else {
					convertedValue = newValue;
				}

				if (!values.contains(convertedValue)) {
					values.add(convertedValue);
				}
			}

		}
		set(metadata, values);

		return (W) this;
	}

	public <T, W extends RecordWrapper> W set(Metadata metadata, T value) {
		ensureConnected();
		return set(metadata.getLocalCode(), value);
	}

	protected boolean getBooleanWithDefaultValue(String param, boolean defaultValue) {
		Boolean value = get(param);
		return value == null ? defaultValue : value;
	}

	protected <T> T getEnumWithDefaultValue(String param, T defaultValue) {
		T value = get(param);
		return value == null ? defaultValue : value;
	}

	public String getId() {
		return wrappedRecord.getId();
	}

	public RecordId getWrappedRecordId() {
		return wrappedRecord.getRecordId();
	}

	public String getSchemaCode() {
		return wrappedRecord.getSchemaCode();
	}

	public long getVersion() {
		return wrappedRecord.getVersion();
	}

	public String getCollection() {
		return wrappedRecord.getCollection();
	}

	public List<String> getPaths() {
		return wrappedRecord.getList(Schemas.PATH);
	}

	public List<String> getParentPaths() {
		return RecordUtils.parentPaths(wrappedRecord);
	}

	public List<String> getRemovedAuthorizations() {
		return wrappedRecord.getList(Schemas.REMOVED_AUTHORIZATIONS);
	}

	public List<String> getAllRemovedAuths() {
		return wrappedRecord.getList(Schemas.ALL_REMOVED_AUTHS);
	}

	public List<String> getAttachedAncestors() {
		return wrappedRecord.getList(Schemas.ATTACHED_ANCESTORS);
	}

	public List<String> getTokens() {
		return wrappedRecord.getList(Schemas.TOKENS);
	}

	public List<String> getManualTokens() {
		return wrappedRecord.getList(Schemas.MANUAL_TOKENS);
	}

	public RecordWrapper setManualTokens(String... manualTokens) {
		wrappedRecord.set(Schemas.MANUAL_TOKENS, Arrays.asList(manualTokens));
		return this;
	}

	public RecordWrapper setManualTokens(List<String> manualTokens) {
		wrappedRecord.set(Schemas.MANUAL_TOKENS, manualTokens);
		return this;
	}

	public String getTitle() {
		return wrappedRecord.get(Schemas.TITLE, locale);
	}

	public String getTitle(Locale locale) {
		return wrappedRecord.get(Schemas.TITLE, locale);
	}

	public RecordWrapper setTitle(String title) {
		wrappedRecord.set(Schemas.TITLE, title);
		return this;
	}

	public RecordWrapper setTitles(Map<Language, String> titles) {
		for (Map.Entry<Language, String> entry : titles.entrySet()) {
			wrappedRecord.set(Schemas.TITLE, entry.getKey().getLocale(), entry.getValue());
		}
		return this;
	}

	public RecordWrapper setTitle(Locale locale, String title) {
		wrappedRecord.set(Schemas.TITLE, locale, title);
		return this;
	}

	public String getLegacyId() {
		return wrappedRecord.get(Schemas.LEGACY_ID);
	}

	public RecordWrapper setLegacyId(String legacyId) {
		wrappedRecord.set(Schemas.LEGACY_ID, legacyId);
		return this;
	}

	public LocalDateTime getCreatedOn() {
		return wrappedRecord.get(Schemas.CREATED_ON);
	}

	public RecordWrapper setCreatedOn(LocalDateTime createdOn) {
		wrappedRecord.set(Schemas.CREATED_ON, createdOn);
		return this;
	}

	public LocalDateTime getModifiedOn() {
		return wrappedRecord.get(Schemas.MODIFIED_ON);
	}

	public RecordWrapper setModifiedOn(LocalDateTime modifiedOn) {
		wrappedRecord.set(Schemas.MODIFIED_ON, modifiedOn);
		return this;
	}

	public String getCreatedBy() {
		return wrappedRecord.get(Schemas.CREATED_BY);
	}

	public RecordWrapper setCreatedBy(String createdBy) {
		wrappedRecord.set(Schemas.CREATED_BY, createdBy);
		return this;
	}


	public RecordWrapper setCreatedBy(User createdBy) {
		wrappedRecord.set(Schemas.CREATED_BY, createdBy);
		return this;
	}

	public String getModifiedBy() {
		return wrappedRecord.get(Schemas.MODIFIED_BY);
	}

	public RecordWrapper setModifiedBy(User modifiedBy) {
		wrappedRecord.set(Schemas.MODIFIED_BY, modifiedBy);
		return this;
	}

	public RecordWrapper setModifiedBy(String modifiedBy) {
		wrappedRecord.set(Schemas.MODIFIED_BY, modifiedBy);
		return this;
	}

	public boolean isDetachedAuthorizations() {
		Boolean value = wrappedRecord.get(Schemas.IS_DETACHED_AUTHORIZATIONS);
		return value == null ? false : value;
	}

	public MetadataSchema getSchema() {
		ensureConnected();
		return types.getSchema(getSchemaCode());
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "types");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "types");
	}

	@Override
	public String toString() {
		return "" + wrappedRecord;
	}

	protected String toStringPrintingCodes(String... metadatas) {
		StringBuilder stringBuilder = new StringBuilder();

		for (String metadata : metadatas) {
			Object value = get(metadata);
			if (value != null) {
				if (stringBuilder.length() > 0) {
					stringBuilder.append(", ");
				}
				stringBuilder.append(metadata);
				stringBuilder.append("=");
				stringBuilder.append(value);
			}
		}

		return wrappedRecord.toString() + " " + stringBuilder.toString();
	}

	public void reconnect(MetadataSchemaTypes metadataSchemaTypes) {
		this.types = metadataSchemaTypes;
	}

	private void writeObject(ObjectOutputStream oos)
			throws IOException {
		oos.writeObject(wrappedRecord);
	}

	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		this.wrappedRecord = (Record) ois.readObject();
	}

	private void ensureConnected() {
		if (types == null) {
			throw new RecordWrapperRuntimeException_CannotUseDisconnectedRecordWrapper();
		}
	}

	public boolean isLogicallyDeletedStatus() {
		return Boolean.TRUE.equals(get(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode()));
	}

	public Boolean getLogicallyDeletedStatus() {
		return get(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode());
	}

	public boolean isMarkedForPreviewConversion() {
		return Boolean.TRUE.equals(get(Schemas.MARKED_FOR_PREVIEW_CONVERSION.getLocalCode()));
	}

	public void setMarkedForPreviewConversion(Boolean value) {
		set(Schemas.MARKED_FOR_PREVIEW_CONVERSION.getLocalCode(), value);
	}

	public Record changeSchemaTo(String newSchemaCode) {

		if (!newSchemaCode.contains("_")) {
			String currentSchemaType = new SchemaUtils().getSchemaTypeCode(wrappedRecord.getSchemaCode());
			newSchemaCode = currentSchemaType + "_" + newSchemaCode;
		}

		MetadataSchema wasSchema = getMetadataSchemaTypes().getSchema(wrappedRecord.getSchemaCode());
		MetadataSchema newSchema = getMetadataSchemaTypes().getSchema(newSchemaCode);
		wrappedRecord.changeSchema(wasSchema, newSchema);
		return wrappedRecord;
	}

	@Override
	public Record get() {
		return wrappedRecord;
	}
}
