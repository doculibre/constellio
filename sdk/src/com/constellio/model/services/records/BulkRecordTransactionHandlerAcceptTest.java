package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.utils.ThreadList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.BulkRecordTransactionHandlerRuntimeException.BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BulkRecordTransactionHandlerAcceptTest extends ConstellioTest {

	AtomicBoolean exceptionTriggered = new AtomicBoolean();

	BulkRecordTransactionHandlerOptions options = new BulkRecordTransactionHandlerOptions();
	@Mock RecordServices recordServices;
	@Mock ThreadList<Thread> zeThreadList;
	@Mock Record aRecord, aCachedRecord, theRecordThrowingAnException, theNextRecord, aReferencedRecord, anotherReferencedRecord, aThirdReferencedRecord;
	@Mock RecordsCaches recordsCaches;
	@Mock RecordsCache zeCollectionRecordsCache;
	BulkRecordTransactionHandler handler;

	@Before
	public void setUp()
			throws Exception {

		when(aRecord.getId()).thenReturn("aRecord");
		when(aCachedRecord.getId()).thenReturn("aCachedRecord");
		when(theRecordThrowingAnException.getId()).thenReturn("theRecordThrowingAnException");
		when(theNextRecord.getId()).thenReturn("theNextRecord");
		when(aReferencedRecord.getId()).thenReturn("aReferencedRecord");
		when(anotherReferencedRecord.getId()).thenReturn("anotherReferencedRecord");
		when(aThirdReferencedRecord.getId()).thenReturn("aThirdReferencedRecord");

		when(aRecord.getSchemaCode()).thenReturn("zeSchema_default");
		when(aCachedRecord.getSchemaCode()).thenReturn("cachedSchema_default");
		when(theRecordThrowingAnException.getSchemaCode()).thenReturn("zeSchema_default");
		when(theNextRecord.getSchemaCode()).thenReturn("zeSchema_default");
		when(aReferencedRecord.getSchemaCode()).thenReturn("zeSchema_default");
		when(anotherReferencedRecord.getSchemaCode()).thenReturn("zeSchema_default");
		when(aThirdReferencedRecord.getSchemaCode()).thenReturn("zeSchema_default");

		when(aRecord.getCollection()).thenReturn(zeCollection);
		when(aCachedRecord.getCollection()).thenReturn(zeCollection);
		when(theRecordThrowingAnException.getCollection()).thenReturn(zeCollection);
		when(theNextRecord.getCollection()).thenReturn(zeCollection);
		when(aReferencedRecord.getCollection()).thenReturn(zeCollection);
		when(anotherReferencedRecord.getCollection()).thenReturn(zeCollection);
		when(aThirdReferencedRecord.getCollection()).thenReturn(zeCollection);

		when(aRecord.get()).thenReturn(aRecord);
		when(aCachedRecord.get()).thenReturn(aCachedRecord);
		when(theRecordThrowingAnException.get()).thenReturn(theRecordThrowingAnException);
		when(theNextRecord.get()).thenReturn(theNextRecord);
		when(aReferencedRecord.get()).thenReturn(aReferencedRecord);
		when(anotherReferencedRecord.get()).thenReturn(anotherReferencedRecord);
		when(aThirdReferencedRecord.get()).thenReturn(aThirdReferencedRecord);

		when(recordServices.getRecordsCaches()).thenReturn(recordsCaches);
		when(recordsCaches.getCache(zeCollection)).thenReturn(zeCollectionRecordsCache);
		when(zeCollectionRecordsCache.isConfigured("zeSchema")).thenReturn(false);
		when(zeCollectionRecordsCache.isConfigured("cachedSchema")).thenReturn(true);

		options = options.withNumberOfThreads(2).withQueueSize(6).withRecordsPerBatch(1);

	}

	@Test
	public void givenARecordServiceRuntimeExceptionOccurWhileExecutingTheSecondTransactionThenContinueAndThrowException()
			throws Exception {

		ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);

		doAnswer(workUntilExceptionTriggered()).doAnswer(triggerException(new RecordServicesRuntimeException("")))
				.when(recordServices).execute(any(Transaction.class));

		handler = new BulkRecordTransactionHandler(recordServices, "BulkRecordTransactionHandlerAcceptTest-test", options);

		handler.append(asList(aRecord), asList(aReferencedRecord));
		handler.append(asList(theRecordThrowingAnException), asList(anotherReferencedRecord, aThirdReferencedRecord));
		handler.append(asList(theNextRecord));

		triggerAnExceptionInAThread();

		try {
			handler.closeAndJoin();
			fail("Exception expected");
		} catch (BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction e) {
			//OK
		}

		verify(recordServices, times(3)).execute(transactionArgumentCaptor.capture());

		Transaction transactionWithARecord = null;
		Transaction transactionWithExceptionRecord = null;
		Transaction transactionWithTheNextRecord = null;

		for (Transaction capturedTransaction : transactionArgumentCaptor.getAllValues()) {
			if (capturedTransaction.getRecords().contains(aRecord)) {
				transactionWithARecord = capturedTransaction;
			} else if (capturedTransaction.getRecords().contains(theNextRecord)) {
				transactionWithTheNextRecord = capturedTransaction;
			} else {
				transactionWithExceptionRecord = capturedTransaction;
			}
		}

		assertThat(transactionWithARecord.getRecords()).containsOnly(aRecord);
		assertThat(transactionWithARecord.getReferencedRecords()).hasSize(1).containsValue(aReferencedRecord);
		assertThat(transactionWithARecord.getRecordUpdateOptions().getRecordsFlushing())
				.isEqualTo(RecordsFlushing.WITHIN_MINUTES(5));

		assertThat(transactionWithTheNextRecord.getRecords()).containsOnly(theNextRecord);
		assertThat(transactionWithARecord.getRecordUpdateOptions().getRecordsFlushing())
				.isEqualTo(RecordsFlushing.WITHIN_MINUTES(5));

		assertThat(transactionWithExceptionRecord.getRecords()).contains(theRecordThrowingAnException);
		assertThat(transactionWithExceptionRecord.getReferencedRecords()).hasSize(2).containsValue(anotherReferencedRecord)
				.containsValue(aThirdReferencedRecord);
		assertThat(transactionWithExceptionRecord.getRecordUpdateOptions().getRecordsFlushing())
				.isEqualTo(RecordsFlushing.WITHIN_MINUTES(5));

	}


	@Test
	public void givenARecordServiceRuntimeExceptionOccurWhenResetExceptionThenHandlerRecovers()
			throws Exception {

		doAnswer(workUntilExceptionTriggered()).doAnswer(triggerException(new RecordServicesRuntimeException("")))
				.when(recordServices).execute(any(Transaction.class));

		handler = new BulkRecordTransactionHandler(recordServices, "BulkRecordTransactionHandlerAcceptTest-test", options);

		handler.append(asList(aRecord), asList(aReferencedRecord));
		handler.append(asList(theRecordThrowingAnException), asList(anotherReferencedRecord, aThirdReferencedRecord));

		triggerAnExceptionInAThread();
		Thread.sleep(100);

		try {
			handler.closeAndJoin();
			fail("Exception expected");
		} catch (BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction e) {
			//OK
		}

		try {
			handler.append(asList(theNextRecord));
			fail("Exception expected");
		} catch (BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction e) {
			//OK
		}

		handler.resetException();

		handler.append(asList(theNextRecord));

		handler.closeAndJoin();
	}

	private void triggerAnExceptionInAThread() {
		exceptionTriggered.set(true);
	}

	private Answer<Object> workUntilExceptionTriggered() {
		return new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				while (!exceptionTriggered.get()) {
					Thread.sleep(100);
				}
				Thread.sleep(200);

				return null;
			}
		};
	}

	private Answer<Object> triggerException(final Exception exception) {
		return new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				while (!exceptionTriggered.get()) {
					Thread.sleep(10);
				}
				throw exception;
			}
		};
	}

}
