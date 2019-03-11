package com.constellio.model.services.batch.actions;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChangeValueOfMetadataBatchProcessAcceptanceTest extends ConstellioTest {
	String changedMetadataCode1 = "type_default_code1";
	String changedMetadataCode2 = "type_default_code2";
	@Mock Metadata changedMetadata1;
	@Mock Metadata changedMetadata2;
	@Mock MetadataSchemaTypes schemaTypes;

	@Mock Record record1;
	@Mock Record record2;
	@Mock Record record3;
	List<Record> batch;
	Map<String, Object> metadataChangedValues;

	ChangeValueOfMetadataBatchProcessAction action;

	@Mock Object changedValue1;
	@Mock Object changedValue2;

	@Mock RecordProvider recordProvider;

	@Before
	public void setUp()
			throws Exception {
		metadataChangedValues = new HashMap<>();
		metadataChangedValues.put(changedMetadataCode1, changedValue1);
		metadataChangedValues.put(changedMetadataCode2, changedValue2);

		action = new ChangeValueOfMetadataBatchProcessAction(metadataChangedValues);

		batch = asList(record1, record2, record3);
		when(record1.getId()).thenReturn(aString());
		when(record2.getId()).thenReturn(aString());
		when(record3.getId()).thenReturn(aString());
		when(record1.getSchemaCode()).thenReturn("type_default");
		when(record2.getSchemaCode()).thenReturn("type_default");
		when(record3.getSchemaCode()).thenReturn("type_default");

		when(schemaTypes.getMetadata(changedMetadataCode1)).thenReturn(changedMetadata1);
		when(schemaTypes.getMetadata(changedMetadataCode2)).thenReturn(changedMetadata2);
	}

	@Test
	public void whenGetParametersThenReturnMetadataChangedValues()
			throws Exception {

		Object[] parameters = action.getInstanceParameters();
		assertThat(parameters).containsOnly(metadataChangedValues);
	}

	@Test
	public void whenExecutingThenCreateTransactionWhichDoesNotSkipRequiredValueValidation()
			throws Exception {

		Transaction transaction = action.execute(batch, null, schemaTypes, recordProvider, getModelLayerFactory());

		assertThat(transaction.getRecords()).containsOnly(record1, record2, record3);
		for (Record record : batch) {
			verify(record).set(changedMetadata1, changedValue1);
			verify(record).set(changedMetadata2, changedValue2);
		}

		assertThat(transaction.isSkippingRequiredValuesValidation()).isFalse();
	}

}
