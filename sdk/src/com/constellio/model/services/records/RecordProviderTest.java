package com.constellio.model.services.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class RecordProviderTest extends ConstellioTest {

	@Mock Record firstRecord;
	String firstRecordId = aString();

	@Mock Record secondRecord;
	String secondRecordId = aString();

	@Mock RecordServices recordServices;

	@Mock Transaction transaction;

	@Before
	public void setUp()
			throws Exception {

		when(firstRecord.getId()).thenReturn(firstRecordId);
		when(secondRecord.getId()).thenReturn(secondRecordId);

		when(recordServices.getDocumentById(firstRecordId)).thenReturn(firstRecord);
		when(recordServices.getDocumentById(secondRecordId)).thenReturn(secondRecord);
		when(recordServices.realtimeGetRecordById(firstRecordId)).thenReturn(firstRecord);
		when(recordServices.realtimeGetRecordById(secondRecordId)).thenReturn(secondRecord);

	}

	@Test
	public void givenRecordProviderWithMetadatasWhenGettingOnOfTheseMetadataThenReturnDoNotCallService()
			throws Exception {

		RecordProvider recordProvider = new RecordProvider(recordServices, null, asList(firstRecord), null);

		assertThat(recordProvider.getRecord(firstRecordId)).isEqualTo(firstRecord);

		verifyZeroInteractions(recordServices);
	}

	@Test
	public void givenRecordProviderWithMetadatasWhenGettingAnotherMetadataThenReturnCallService()
			throws Exception {

		RecordProvider recordProvider = new RecordProvider(recordServices, null, asList(firstRecord), null);

		assertThat(recordProvider.getRecord(secondRecordId)).isEqualTo(secondRecord);

		verify(recordServices).getDocumentById(secondRecordId);
	}

	@Test
	public void givenRecordProviderCreatedFromAnotherOneWithMetadatasWhenGettingOnOfTheseMetadataThenReturnDoNotCallService()
			throws Exception {

		RecordProvider nestedRecordProvider = new RecordProvider(recordServices, null, asList(firstRecord), null);
		RecordProvider recordProvider = new RecordProvider(recordServices, nestedRecordProvider, asList(secondRecord), null);
		assertThat(recordProvider.getRecord(firstRecordId)).isEqualTo(firstRecord);
		assertThat(recordProvider.getRecord(secondRecordId)).isEqualTo(secondRecord);

		verifyZeroInteractions(recordServices);
	}

	@Test
	public void givenRecordInMemoryListWhenCheckingSingleValueInMemoryListThenTrue() {
		RecordProvider recordProvider = new RecordProvider(recordServices, null, asList(firstRecord), null);

		assertThat(recordProvider.hasRecordInMemoryList(firstRecordId)).isTrue();
	}

	@Test
	public void givenRecordInMemoryListWhenCheckingMultiValueInMemoryListThenTrue() {
		RecordProvider recordProvider = new RecordProvider(recordServices, null, asList(firstRecord), null);

		assertThat(recordProvider.hasRecordInMemoryList(asList(firstRecordId))).isTrue();
	}

	@Test
	public void givenRecordNotInMemoryListWhenCheckingSingleValueInMemoryListThenFalse() {
		RecordProvider recordProvider = new RecordProvider(recordServices, null, asList(firstRecord), null);

		assertThat(recordProvider.hasRecordInMemoryList(secondRecordId)).isFalse();
	}

	@Test
	public void givenRecordNotMemoryListWhenCheckingMultiValueInMemoryListThenFalse() {
		RecordProvider recordProvider = new RecordProvider(recordServices, null, asList(firstRecord), null);

		assertThat(recordProvider.hasRecordInMemoryList(asList(secondRecordId))).isFalse();
	}

	@Test
	public void givenTransactionWithReferencedRecordThenCopiedInMemoryList() {

		Map<String, Record> referencedRecords = new HashMap<>();
		referencedRecords.put(firstRecordId, firstRecord);
		referencedRecords.put(secondRecordId, secondRecord);
		when(transaction.getReferencedRecords()).thenReturn(referencedRecords);
		RecordProvider recordProvider = new RecordProvider(recordServices, null, new ArrayList<Record>(), transaction);

		assertThat(recordProvider.hasRecordInMemoryList(asList(firstRecordId))).isTrue();
		assertThat(recordProvider.hasRecordInMemoryList(asList(secondRecordId))).isTrue();
		assertThat(recordProvider.getRecord(firstRecordId)).isEqualTo(firstRecord);
		assertThat(recordProvider.getRecord(secondRecordId)).isEqualTo(secondRecord);
	}

	@Test
	public void givenSameRecordReferencedInTransactionAndModifiedThenModifiedInstanceInMemoryList() {

		Map<String, Record> referencedRecords = new HashMap<>();
		referencedRecords.put(firstRecordId, firstRecord);
		referencedRecords.put(secondRecordId, secondRecord);
		when(transaction.getReferencedRecords()).thenReturn(referencedRecords);
		Record modifiedSecondRecord = mock(Record.class);
		when(modifiedSecondRecord.getId()).thenReturn(secondRecordId);
		RecordProvider recordProvider = new RecordProvider(recordServices, null, Arrays.asList(modifiedSecondRecord),
				transaction);

		assertThat(recordProvider.hasRecordInMemoryList(asList(firstRecordId))).isTrue();
		assertThat(recordProvider.hasRecordInMemoryList(asList(secondRecordId))).isTrue();
		assertThat(recordProvider.getRecord(firstRecordId)).isEqualTo(firstRecord);
		assertThat(recordProvider.getRecord(secondRecordId)).isEqualTo(modifiedSecondRecord);
	}

}
