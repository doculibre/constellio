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
package com.constellio.model.services.records;

import static com.constellio.sdk.tests.TestUtils.anInteger;
import static com.constellio.sdk.tests.TestUtils.mockMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import sun.security.krb5.Config;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecordValidationServicesTest extends ConstellioTest {

	@Mock RecordProvider recordProvider;

	@Mock MetadataSchemasManager schemasManager;

	@Mock MetadataSchemaTypes types;

	@Mock MetadataSchema schema;

	@Mock SearchServices searchServices;

	@Mock ConfigProvider configProvider;

	@Mock Transaction transaction;

	Metadata firstMetadata = mockMetadata("zeSchema_default_first");
	Metadata secondMetadata = mockMetadata("zeSchema_default_second");
	Metadata thirdMetadata = mockMetadata("zeSchema_default_third");
	Metadata multiValueMetadata = mockMetadata("zeSchema_default_multivalueMetadata");

	@Mock DataEntry manualDataEntry;
	@Mock DataEntry copiedDataEntry;
	@Mock DataEntry calculatedDataEntry;

	@Mock RecordMetadataValidator<Object> validator1;
	@Mock RecordMetadataValidator<Object> validator2;
	@Mock RecordMetadataValidator<Object> validator3;

	@Mock Record record;
	RecordValidationServices services;

	String aStringValue = "aStringValue";

	@Before
	public void setUp() {

		when(record.getCollection()).thenReturn(zeCollection);
		when(schemasManager.getSchemaTypes(zeCollection)).thenReturn(types);
		when(types.getSchema("aSchemaCode")).thenReturn(schema);

		MetadataList metadatas = new MetadataList(firstMetadata, secondMetadata, thirdMetadata);

		when(schema.getMetadatas()).thenReturn(metadatas);

		Set<RecordMetadataValidator<?>> validators1 = new HashSet<>();
		validators1.add(validator1);

		when(firstMetadata.getValidators()).thenReturn(validators1);

		Set<RecordMetadataValidator<?>> validators2 = new HashSet<>();
		validators2.add(validator2);

		when(secondMetadata.getValidators()).thenReturn(validators2);

		Set<RecordMetadataValidator<?>> validators3 = new HashSet<>();
		validators3.add(validator3);

		when(thirdMetadata.getValidators()).thenReturn(validators3);

		when(record.getSchemaCode()).thenReturn("aSchemaCode");
		when(record.get(firstMetadata)).thenReturn(aStringValue);
		when(record.get(secondMetadata)).thenReturn(aStringValue);
		when(record.get(thirdMetadata)).thenReturn(aStringValue);

		when(manualDataEntry.getType()).thenReturn(DataEntryType.MANUAL);
		when(copiedDataEntry.getType()).thenReturn(DataEntryType.COPIED);
		when(calculatedDataEntry.getType()).thenReturn(DataEntryType.CALCULATED);

		services = spy(new RecordValidationServices(configProvider, schemasManager, searchServices));
		doReturn(true).when(services).hasSecurityOnSchema(record);
	}

	@Test
	public void whenValidatingUsingCustomSchemasThenAllValidated()
			throws Exception {

		services.validateSchemaUsingCustomSchemaValidator(record, recordProvider, transaction);

		verify(validator1).validate(eq(firstMetadata), eq(aStringValue), any(ConfigProvider.class), any(ValidationErrors.class));
		verify(validator2).validate(eq(secondMetadata), eq(aStringValue), any(ConfigProvider.class), any(ValidationErrors.class));
		verify(validator3).validate(eq(thirdMetadata), eq(aStringValue), any(ConfigProvider.class), any(ValidationErrors.class));
	}

	@Test
	public void whenValidatingManualMetadatasdThenAutomaticMetadatasNotValidated()
			throws Exception {
		when(firstMetadata.getDataEntry()).thenReturn(manualDataEntry);
		when(secondMetadata.getDataEntry()).thenReturn(copiedDataEntry);
		when(thirdMetadata.getDataEntry()).thenReturn(calculatedDataEntry);

		services.validateManualMetadatas(record, recordProvider, transaction);

		assertThat(services.getManualMetadatas(schema)).containsExactly(firstMetadata);
	}
}
