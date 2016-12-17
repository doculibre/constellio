package com.constellio.model.services.batch.actions;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.sdk.tests.ConstellioTest;

public class ReindexMetadatasBatchProcessActionTest extends ConstellioTest {

	@Mock List<String> reindexedMetadataCodes;
	@Mock MetadataList reindexedMetadatas;
	@Mock MetadataSchemaTypes schemaTypes;

	@Mock Record record1;
	@Mock Record record2;
	@Mock Record record3;

	@Mock RecordProvider recordProvider;

	List<Record> batch;

	ReindexMetadatasBatchProcessAction action;

	@Before
	public void setUp()
			throws Exception {
		action = new ReindexMetadatasBatchProcessAction(reindexedMetadataCodes);

		batch = asList(record1, record2, record3);
		when(record1.getId()).thenReturn(aString());
		when(record2.getId()).thenReturn(aString());
		when(record3.getId()).thenReturn(aString());

		when(schemaTypes.getMetadatas(reindexedMetadataCodes)).thenReturn(reindexedMetadatas);
	}

	@Test
	public void whenGetParametersThenReturnMetadataCodes()
			throws Exception {

		Object[] parameters = action.getInstanceParameters();
		assertThat(parameters).containsOnly(reindexedMetadataCodes);
	}

	@Test
	public void whenExecutingThenCreateTransactionSetForcedReindexedFieldsOptionsAndAddRecords()
			throws Exception {

		Transaction transaction = action.execute(batch, schemaTypes, recordProvider);

		assertThat(transaction.getRecordUpdateOptions().getTransactionRecordsReindexation())
				.isEqualTo(new TransactionRecordsReindexation(reindexedMetadatas));
		assertThat(transaction.getRecords()).containsOnly(record1, record2, record3);
	}

}
