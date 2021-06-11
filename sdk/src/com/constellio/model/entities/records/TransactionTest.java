package com.constellio.model.entities.records;

import com.constellio.model.entities.records.TransactionRuntimeException.RecordIdCollision;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TransactionTest extends ConstellioTest {

	@Mock Record record1, record2, record3;

	Transaction transaction;

	@Before
	public void setUp()
			throws Exception {
		transaction = spy(new Transaction());
	}

	@Test
	public void givenRecordHasAnIdWhenAddingUpdateThenRecordUpdated()
			throws Exception {
		when(record1.getId()).thenReturn("id1");
		when(record1.isSaved()).thenReturn(true);

		transaction.addUpdate(record1);

		verify(transaction).update(record1);
	}

	@Test
	public void givenRecordHasNoIdWhenAddingUpdateThenRecordUpdated()
			throws Exception {
		transaction.addUpdate(record1);

		assertThat(transaction.getRecords()).containsOnly(record1);
	}

	@Test
	public void whenAddingMultipleUpdatesThenAllRecordUpdated()
			throws Exception {
		transaction.addUpdate(asList(record1, record2, record3));

		verify(transaction).addUpdate(record1);
		verify(transaction).addUpdate(record2);
		verify(transaction).addUpdate(record3);
	}

	@Test
	public void whenAddingListOfUpdatesThenAllRecordUpdated()
			throws Exception {
		transaction.addUpdate(Arrays.asList(record1, record2, record3));

		verify(transaction).addUpdate(record1);
		verify(transaction).addUpdate(record2);
		verify(transaction).addUpdate(record3);
	}

	@Test
	public void givenRecordNotInUpdatedMapWhenUpdatingThenRecordAddedToMapAndRecords()
			throws Exception {
		when(record1.getId()).thenReturn("id1");

		transaction.update(record1);

		assertThat(transaction.getRecords()).containsOnly(record1);
		assertThat(transaction.updatedRecordsMap).containsEntry("id1", record1);
	}

	@Test(expected = RecordIdCollision.class)
	public void givenIdCollisionWhenUpdatingThenExceptionThrown()
			throws Exception {
		when(record1.getId()).thenReturn("id1");
		when(record2.getId()).thenReturn("id1");

		transaction.update(record1);
		transaction.update(record2);
	}

	@Test
	public void givenRecordsNotInUpdatedMapWhenUpdatingMultipleRecordsThenRecordsAddedToMapAndRecords()
			throws Exception {
		when(record1.getId()).thenReturn("id1");
		when(record2.getId()).thenReturn("id2");
		when(record3.getId()).thenReturn("id3");

		transaction.update(asList(record1, record2, record3));

		assertThat(transaction.getRecords()).containsOnly(record1, record2, record3);
		assertThat(transaction.updatedRecordsMap).containsEntry("id1", record1).containsEntry("id2", record2)
				.containsEntry("id3", record3);
	}

	@Test
	public void givenRecordsNotInUpdatedMapWhenUpdatingListOfRecordsThenRecordsAddedToMapAndRecords()
			throws Exception {
		when(record1.getId()).thenReturn("id1");
		when(record2.getId()).thenReturn("id2");
		when(record3.getId()).thenReturn("id3");

		transaction.update(Arrays.asList(record1, record2, record3));

		assertThat(transaction.getRecords()).containsOnly(record1, record2, record3);
		assertThat(transaction.updatedRecordsMap).containsEntry("id1", record1).containsEntry("id2", record2)
				.containsEntry("id3", record3);
	}

	// ---------
	@Test
	public void whenAddUpdateRecordThenValidateCollections()
			throws Exception {
		when(record1.getId()).thenReturn("id1");
		when(record1.getCollection()).thenReturn("collection1");
		doNothing().when(transaction).validateCollection(anyString(), anyString());

		transaction.addUpdate(record1);

		verify(transaction, times(1)).validateCollection("collection1", record1.getId());
	}

	@Test
	public void whenAddUpdateRecordsThenValidateCollections()
			throws Exception {
		when(record1.getId()).thenReturn("id1");
		when(record2.getId()).thenReturn("id2");
		when(record3.getId()).thenReturn("id3");
		when(record1.getCollection()).thenReturn("collection1");
		when(record2.getCollection()).thenReturn("collection1");
		when(record3.getCollection()).thenReturn("collection2");
		doNothing().when(transaction).validateCollection(anyString(), anyString());

		transaction.addUpdate(record1, record2, record3);

		verify(transaction, times(1)).validateCollection("collection1", record1.getId());
		verify(transaction, times(1)).validateCollection("collection1", record2.getId());
		verify(transaction, times(1)).validateCollection("collection2", record3.getId());
	}

	@Test
	public void whenAddUpdateRecordsListThenValidateCollections()
			throws Exception {
		when(record1.getId()).thenReturn("id1");
		when(record2.getId()).thenReturn("id2");
		when(record3.getId()).thenReturn("id3");
		when(record1.getCollection()).thenReturn("collection1");
		when(record2.getCollection()).thenReturn("collection1");
		when(record3.getCollection()).thenReturn("collection2");
		doNothing().when(transaction).validateCollection(anyString(),anyString());

		transaction.addUpdate(Arrays.asList(record1, record2, record3));

		verify(transaction, times(1)).validateCollection("collection1",record1.getId());
		verify(transaction, times(1)).validateCollection("collection1",record2.getId());
		verify(transaction, times(1)).validateCollection("collection2", record3.getId());
	}

	@Test
	public void whenAddRecordThenValidateCollections()
			throws Exception {
		when(record1.getId()).thenReturn("id1");
		when(record1.getCollection()).thenReturn("collection1");
		doNothing().when(transaction).validateCollection(anyString(), anyString());

		transaction.add(record1);

		verify(transaction, times(1)).validateCollection("collection1", record1.getId());
	}

	@Test
	public void whenUpdateRecordThenValidateCollections()
			throws Exception {
		when(record1.getId()).thenReturn("id1");
		when(record1.getCollection()).thenReturn("collection1");
		doNothing().when(transaction).validateCollection(anyString(),anyString());

		transaction.update(record1);

		verify(transaction, times(1)).validateCollection("collection1",record1.getId());
	}

	@Test
	public void whenUpdateRecordsThenValidateCollections()
			throws Exception {
		when(record1.getId()).thenReturn("id1");
		when(record2.getId()).thenReturn("id2");
		when(record3.getId()).thenReturn("id3");
		when(record1.getCollection()).thenReturn("collection1");
		when(record2.getCollection()).thenReturn("collection1");
		when(record3.getCollection()).thenReturn("collection2");
		doNothing().when(transaction).validateCollection(anyString(),anyString());

		transaction.update(record1, record2, record3);

		verify(transaction, times(1)).validateCollection("collection1",record1.getId());
		verify(transaction, times(1)).validateCollection("collection1",record2.getId());
		verify(transaction, times(1)).validateCollection("collection2", record3.getId());
	}

	@Test
	public void whenUpdateRecordsListThenValidateCollections()
			throws Exception {
		when(record1.getId()).thenReturn("id1");
		when(record2.getId()).thenReturn("id2");
		when(record3.getId()).thenReturn("id3");
		when(record1.getCollection()).thenReturn("collection1");
		when(record2.getCollection()).thenReturn("collection1");
		when(record3.getCollection()).thenReturn("collection2");
		doNothing().when(transaction).validateCollection(anyString(), anyString());

		transaction.update(Arrays.asList(record1, record2, record3));

		verify(transaction, times(1)).validateCollection("collection1", record1.getId());
		verify(transaction, times(1)).validateCollection("collection1", record2.getId());
		verify(transaction, times(1)).validateCollection("collection2", record3.getId());
	}

	@Test(expected = TransactionRuntimeException.DifferentCollectionsInRecords.class)
	public void whenNewTransactionWithRecordsThenValidateCollectionsFromRecordsList()
			throws Exception {
		when(record1.getId()).thenReturn("id1");
		when(record2.getId()).thenReturn("id2");
		when(record3.getId()).thenReturn("id3");
		when(record1.getCollection()).thenReturn("collection1");
		when(record2.getCollection()).thenReturn("collection1");
		when(record3.getCollection()).thenReturn("collection2");

		transaction = new Transaction(record1, record2, record3);
	}

	@Test
	public void whenCopyingTransactionThenAllOptionsAndFlagsCopied() {
		transaction.setSkippingRequiredValuesValidation(true);
		transaction.setSkippingReferenceToLogicallyDeletedValidation(true);

		Transaction newTransaction = new Transaction(transaction);

		assertThat(newTransaction.isSkippingRequiredValuesValidation()).isTrue();
		assertThat(newTransaction.isSkippingReferenceToLogicallyDeletedValidation()).isTrue();
	}

	@Test
	public void whenDuplicatingTransactionThenDuplicateIdsToReindex() {
		transaction.addRecordToReindex("id1");
		transaction.addRecordToReindex("id2");

		Transaction newTransaction = new Transaction(transaction);

		assertThat(newTransaction.getIdsToReindex()).containsOnly("id1", "id2");
	}
}
