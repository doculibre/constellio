/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.entities.records.wrappers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.CollectionObject;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException.MetadataSchemaTypesMustBeNotNull;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException.RecordWrapperRuntimeException_CannotUseDisconnectedRecordWrapper;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException.WrappedRecordMustBeNotNull;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException.WrappedRecordMustMeetRequirements;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;

public class RecordWrapper implements Serializable, CollectionObject {

	public static final String TITLE = Schemas.TITLE_CODE;

	Record wrappedRecord;

	MetadataSchemaTypes types;

	public RecordWrapper(Record record, MetadataSchemaTypes types, String typeRequirement) {
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
		this.types = types;
		this.wrappedRecord = record;
	}

	public Record getWrappedRecord() {
		return wrappedRecord;
	}

	public MetadataSchemaTypes getMetadataSchemaTypes() {
		return types;
	}

	public <T> T get(String localCode) {
		ensureConnected();
		String code = wrappedRecord.getSchemaCode() + "_" + localCode;
		Metadata metadata = types.getMetadata(code);
		return wrappedRecord.get(metadata);
	}

	public <T> List<T> getList(String localCode) {
		ensureConnected();
		String code = wrappedRecord.getSchemaCode() + "_" + localCode;
		Metadata metadata = types.getMetadata(code);
		return wrappedRecord.getList(metadata);
	}

	public <T> RecordWrapper set(String localCode, T value) {
		ensureConnected();
		String code = wrappedRecord.getSchemaCode() + "_" + localCode;
		Metadata metadata = types.getMetadata(code);
		wrappedRecord.set(metadata, value);
		return this;
	}

	protected boolean getBooleanWithDefaultValue(String param, boolean defaultValue) {
		Boolean value = get(param);
		return value == null ? defaultValue : value;
	}

	public String getId() {
		return wrappedRecord.getId();
	}

	public String getSchemaCode() {
		return wrappedRecord.getSchemaCode();
	}

	public long getVersion() {
		return wrappedRecord.getVersion();
	}

	public String getCollection() {
		return wrappedRecord.get(Schemas.COLLECTION);
	}

	public List<String> getPaths() {
		return wrappedRecord.getList(Schemas.PATH);
	}

	public List<String> getParentPaths() {
		return wrappedRecord.getList(Schemas.PARENT_PATH);
	}

	public List<String> getAuthorizations() {
		return wrappedRecord.getList(Schemas.AUTHORIZATIONS);
	}

	public List<String> getRemovedAuthorizations() {
		return wrappedRecord.getList(Schemas.REMOVED_AUTHORIZATIONS);
	}

	public List<String> getInheritedAuthorizations() {
		return wrappedRecord.getList(Schemas.INHERITED_AUTHORIZATIONS);
	}

	public List<String> getAllAuthorizations() {
		return wrappedRecord.getList(Schemas.ALL_AUTHORIZATIONS);
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

	public List<String> getFollowers() {
		return wrappedRecord.getList(Schemas.FOLLOWERS);
	}

	public String getTitle() {
		return wrappedRecord.get(Schemas.TITLE);
	}

	public RecordWrapper setTitle(String title) {
		wrappedRecord.set(Schemas.TITLE, title);
		return this;
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

	public String getModifiedBy() {
		return wrappedRecord.get(Schemas.MODIFIED_BY);
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

}
