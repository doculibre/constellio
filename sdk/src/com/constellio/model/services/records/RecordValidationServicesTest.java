package com.constellio.model.services.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Set;

import static com.constellio.sdk.tests.TestUtils.mockMetadata;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecordValidationServicesTest extends ConstellioTest {

	@Mock RecordProvider recordProvider;

	@Mock MetadataSchemasManager schemasManager;

	@Mock MetadataSchemaTypes types;

	@Mock MetadataSchema schema;

	@Mock SearchServices searchServices;

	@Mock ConfigProvider configProvider;

	@Mock Transaction transaction;

	@Mock RecordUpdateOptions recordUpdateOptions;

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
		when(firstMetadata.getType()).thenReturn(MetadataValueType.STRING);
		when(record.get(secondMetadata)).thenReturn(aStringValue);
		when(secondMetadata.getType()).thenReturn(MetadataValueType.STRING);
		when(record.get(thirdMetadata)).thenReturn(aStringValue);
		when(thirdMetadata.getType()).thenReturn(MetadataValueType.STRING);

		when(manualDataEntry.getType()).thenReturn(DataEntryType.MANUAL);
		when(copiedDataEntry.getType()).thenReturn(DataEntryType.COPIED);
		when(calculatedDataEntry.getType()).thenReturn(DataEntryType.CALCULATED);
		when(transaction.getRecordUpdateOptions()).thenReturn(recordUpdateOptions);
		services = spy(new RecordValidationServices(configProvider, recordProvider, schemasManager, searchServices));
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
