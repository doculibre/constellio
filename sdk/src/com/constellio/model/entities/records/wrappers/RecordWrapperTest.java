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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.SerializationUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException.RecordWrapperRuntimeException_CannotUseDisconnectedRecordWrapper;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.ManualDataEntry;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;

public class RecordWrapperTest extends ConstellioTest {

	String zeMetadataLocalCode = "zeMetadata";
	String zeMetadataCode = "folder_default_zeMetadata";
	LocalDateTime shishOClock = new LocalDateTime();

	@Mock MetadataSchemaTypes metadataSchemaTypes;

	@Mock Record wrappedRecord;

	@Mock Metadata zeMetadata;

	String type = "type";
	String schema = "type_custom";

	RecordWrapper wrapper;

	@Before
	public void setUp()
			throws Exception {

		when(metadataSchemaTypes.getMetadata(zeMetadataCode)).thenReturn(zeMetadata);
		when(wrappedRecord.getSchemaCode()).thenReturn(schema);
		when(zeMetadata.getCode()).thenReturn(zeMetadataCode);
		when(zeMetadata.getLocalCode()).thenReturn(zeMetadataLocalCode);
		when(zeMetadata.getDataEntry()).thenReturn(new ManualDataEntry());

		wrapper = new RecordWrapper(wrappedRecord, metadataSchemaTypes, type);
	}

	@Test(expected = RecordWrapperRuntimeException.WrappedRecordMustBeNotNull.class)
	public void givenRecordWrapperWithNullWrappedRecordThenException()
			throws Exception {

		new RecordWrapper(null, metadataSchemaTypes, type);

	}

	@Test(expected = RecordWrapperRuntimeException.MetadataSchemaTypesMustBeNotNull.class)
	public void givenRecordWrapperWithNullMetadataSchemaTypesThenException()
			throws Exception {

		new RecordWrapper(wrappedRecord, null, type);

	}

	@Test(expected = RecordWrapperRuntimeException.WrappedRecordMustMeetRequirements.class)
	public void givenRecordWrapperWithRecordOfInvalidTypeThenException()
			throws Exception {

		new RecordWrapper(wrappedRecord, metadataSchemaTypes, "othertype");

	}

	@Test(expected = RecordWrapperRuntimeException.WrappedRecordMustMeetRequirements.class)
	public void givenRecordWrapperWithRecordOfInvalidSchemaThenException()
			throws Exception {

		new RecordWrapper(wrappedRecord, metadataSchemaTypes, "type_default");

	}

	@Test
	public void whenGetPropertyThenGetMetadataAndCallRecordWithIt()
			throws Exception {

		when(metadataSchemaTypes.getMetadata("type_custom_zeMeta")).thenReturn(zeMetadata);
		when(wrappedRecord.get(zeMetadata)).thenReturn("42");

		assertThat(wrapper.get("zeMeta")).isEqualTo("42");

	}

	@Test
	public void whenSetPropertyThenGetMetadataAndCallRecordWithIt()
			throws Exception {

		when(metadataSchemaTypes.getMetadata("type_custom_zeMeta")).thenReturn(zeMetadata);

		wrapper.set("zeMeta", "42");

		verify(wrappedRecord).set(zeMetadata, "42");

	}

	@Test
	public void whenSerializingThenCorrectlySerialized() {

		RecordImpl wrappedRecord = new TestRecord("folder_default", "zeUltimateCollection", "42");
		wrappedRecord.set(Schemas.TITLE, "zeTitle");
		wrappedRecord.set(Schemas.MODIFIED_BY, shishOClock);

		wrapper = new RecordWrapper(wrappedRecord, metadataSchemaTypes, "folder");
		byte[] bytes = SerializationUtils.serialize(wrapper);
		RecordWrapper unserializedRecordWrapper = (RecordWrapper) SerializationUtils.deserialize(bytes);

		assertThat(unserializedRecordWrapper.getWrappedRecord()).isEqualTo(wrappedRecord);
		assertThat(unserializedRecordWrapper.getMetadataSchemaTypes()).isNull();

	}

	@Test(expected = RecordWrapperRuntimeException_CannotUseDisconnectedRecordWrapper.class)
	public void givenDisconnectedUnserializedRecordWrapperWhenSetMetadataThenException() {
		RecordImpl wrappedRecord = new TestRecord("folder_default", "zeUltimateCollection", "42");
		wrappedRecord.set(Schemas.TITLE, "zeTitle");
		wrappedRecord.set(Schemas.MODIFIED_BY, shishOClock);

		wrapper = new RecordWrapper(wrappedRecord, metadataSchemaTypes, "folder");
		byte[] bytes = SerializationUtils.serialize(wrapper);
		RecordWrapper unserializedRecordWrapper = (RecordWrapper) SerializationUtils.deserialize(bytes);

		unserializedRecordWrapper.set(zeMetadataLocalCode, "aTitle");
	}

	@Test(expected = RecordWrapperRuntimeException_CannotUseDisconnectedRecordWrapper.class)
	public void givenDisconnectedUnserializedRecordWrapperWhenGetMetadataThenException() {
		RecordImpl wrappedRecord = new TestRecord("folder_default", "zeUltimateCollection", "42");
		wrappedRecord.set(Schemas.TITLE, "zeTitle");
		wrappedRecord.set(Schemas.MODIFIED_BY, shishOClock);

		wrapper = new RecordWrapper(wrappedRecord, metadataSchemaTypes, "folder");
		byte[] bytes = SerializationUtils.serialize(wrapper);
		RecordWrapper unserializedRecordWrapper = (RecordWrapper) SerializationUtils.deserialize(bytes);

		unserializedRecordWrapper.get(zeMetadataLocalCode);
	}

	@Test(expected = RecordWrapperRuntimeException_CannotUseDisconnectedRecordWrapper.class)
	public void givenDisconnectedUnserializedRecordWrapperWhenGetListMetadataThenException() {
		RecordImpl wrappedRecord = new TestRecord("folder_default", "zeUltimateCollection", "42");
		wrappedRecord.set(Schemas.TITLE, "zeTitle");
		wrappedRecord.set(Schemas.MODIFIED_BY, shishOClock);

		wrapper = new RecordWrapper(wrappedRecord, metadataSchemaTypes, "folder");
		byte[] bytes = SerializationUtils.serialize(wrapper);
		RecordWrapper unserializedRecordWrapper = (RecordWrapper) SerializationUtils.deserialize(bytes);

		unserializedRecordWrapper.getList(zeMetadataLocalCode);
	}

	@Test(expected = RecordWrapperRuntimeException_CannotUseDisconnectedRecordWrapper.class)
	public void givenDisconnectedUnserializedRecordWrapperWhenGetSchemaThenException() {
		RecordImpl wrappedRecord = new TestRecord("folder_default", "zeUltimateCollection", "42");
		wrappedRecord.set(Schemas.TITLE, "zeTitle");
		wrappedRecord.set(Schemas.MODIFIED_BY, shishOClock);

		wrapper = new RecordWrapper(wrappedRecord, metadataSchemaTypes, "folder");
		byte[] bytes = SerializationUtils.serialize(wrapper);
		RecordWrapper unserializedRecordWrapper = (RecordWrapper) SerializationUtils.deserialize(bytes);

		unserializedRecordWrapper.getSchema();
	}

	@Test
	public void givenReconnectgivenDisconnectedUnserializedRecordWrapperWhenGetOrSetMetadataThenOK() {
		RecordImpl wrappedRecord = new TestRecord("folder_default", "zeUltimateCollection", "42");
		wrappedRecord.set(Schemas.TITLE, "zeTitle");
		wrappedRecord.set(Schemas.MODIFIED_BY, shishOClock);

		wrapper = new RecordWrapper(wrappedRecord, metadataSchemaTypes, "folder");
		byte[] bytes = SerializationUtils.serialize(wrapper);
		RecordWrapper unserializedRecordWrapper = (RecordWrapper) SerializationUtils.deserialize(bytes);

		unserializedRecordWrapper.reconnect(metadataSchemaTypes);
		unserializedRecordWrapper.get(zeMetadataLocalCode);
		unserializedRecordWrapper.set(zeMetadataLocalCode, "newTitle");
	}
}
