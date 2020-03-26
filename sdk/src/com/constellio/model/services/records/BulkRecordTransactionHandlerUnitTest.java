package com.constellio.model.services.records;

import com.constellio.data.utils.ThreadList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.BulkRecordTransactionHandler.BulkRecordTransactionHandlerTask;
import com.constellio.model.services.records.BulkRecordTransactionHandlerRuntimeException.BulkRecordTransactionHandlerRuntimeException_Interrupted;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class BulkRecordTransactionHandlerUnitTest extends ConstellioTest {

	BulkRecordTransactionHandlerOptions options = new BulkRecordTransactionHandlerOptions();
	@Mock RecordServices recordServices;
	@Mock ThreadList<Thread> zeThreadList;
	@Mock Record record1, record2, record3;

	BulkRecordTransactionHandler handler;

	@Before
	public void setUp()
			throws Exception {
		options = options.withNumberOfThreads(3).withQueueSize(6).withRecordsPerBatch(2);
		handler = new BulkRecordTransactionHandler(recordServices, "test", options) {
			@Override
			ThreadList<Thread> createThreadsAndStartThem() {
				return zeThreadList;
			}
		};

	}

	@Test
	public void whenCreateHandlerThenCreateAndStartThreads()
			throws Exception {

		assertThat(handler.threadList).isSameAs(zeThreadList);
	}

	@Test
	public void whenCreateHandlerThenQueueAsCorrectSize()
			throws Exception {

		assertThat(handler.tasks.remainingCapacity()).isEqualTo(6);
	}

	@Test(expected = BulkRecordTransactionHandlerRuntimeException_Interrupted.class)
	public void givenThreadCannotBeStartedThenThrowException()
			throws Exception {

		doThrow(InterruptedException.class).when(zeThreadList).startAll();

		new BulkRecordTransactionHandler(recordServices, "test", options) {
			@Override
			ThreadList<Thread> createThreadsAndStartThem()
					throws InterruptedException {
				throw new InterruptedException();
			}
		};

	}

	@Test
	public void whenAddingARecordToAnEmptyCurrentListThenAdded()
			throws RecordServicesException {
		handler.append(record1);

		assertThat(handler.tasks).isEmpty();
		assertThat(handler.currentRecords).containsOnly(record1);
	}

	@Test
	public void whenAddingRecordsToAnEmptyCurrentListThenAdded()
			throws RecordServicesException {
		handler.append(asList(record1, record2));

		assertThat(handler.tasks).isEmpty();
		assertThat(handler.currentRecords).containsOnly(record1, record2);
	}

	@Test
	public void whenAddingRecordsExceedingLimitThenStillAddThemInTheSameTransaction()
			throws RecordServicesException {
		handler.append(asList(record1, record2, record3));

		assertThat(handler.tasks).isEmpty();
		assertThat(handler.currentRecords).containsOnly(record1, record2, record3);
	}

	@Test
	public void whenAddingARecordToANonEmptyCurrentListThenAdded()
			throws RecordServicesException {
		handler.options.maxRecordsTotalSizePerBatch = 10;
		handler.append(record1);

		handler.append(record2);

		assertThat(handler.tasks).isEmpty();
		assertThat(handler.currentRecords).containsOnly(record1, record2);
	}

	@Test
	public void whenAddingRecordsToANonEmptyCurrentListThenAdded()
			throws RecordServicesException {
		handler.append(record1);

		handler.append(asList(record2, record3));

		assertThat(handler.currentRecords).containsOnly(record2, record3);
		assertThat(handler.tasks).containsOnly(task(asList(record1)));
	}

	@Test
	public void whenClosingThenAddCurrentToTaskListAndJoinThreads()
			throws Exception {
		handler.append(record1);

		handler.closeAndJoin();

		verify(zeThreadList).joinAll();
		assertThat(handler.tasks).containsExactly(task(asList(record1)), BulkRecordTransactionHandler.NO_MORE_TASKS,
				BulkRecordTransactionHandler.NO_MORE_TASKS, BulkRecordTransactionHandler.NO_MORE_TASKS);

	}

	private BulkRecordTransactionHandlerTask task(List<Record> records) {
		return new BulkRecordTransactionHandlerTask(records, new HashMap<String, Record>());
	}

}
