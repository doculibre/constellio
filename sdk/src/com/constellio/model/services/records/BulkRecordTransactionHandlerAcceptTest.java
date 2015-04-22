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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.constellio.data.utils.ThreadList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.BulkRecordTransactionHandlerRuntimeException.BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction;
import com.constellio.sdk.tests.ConstellioTest;

public class BulkRecordTransactionHandlerAcceptTest extends ConstellioTest {

	AtomicBoolean exceptionTriggered = new AtomicBoolean();

	BulkRecordTransactionHandlerOptions options = new BulkRecordTransactionHandlerOptions();
	@Mock RecordServices recordServices;
	@Mock ThreadList<Thread> zeThreadList;
	@Mock Record aRecord, theRecordThrowingAnException, theNextRecord, aReferencedRecord, anotherReferencedRecord, aThirdReferencedRecord;

	BulkRecordTransactionHandler handler;

	@Before
	public void setUp()
			throws Exception {

		when(aRecord.getId()).thenReturn("aRecord");
		when(theRecordThrowingAnException.getId()).thenReturn("theRecordThrowingAnException");
		when(theNextRecord.getId()).thenReturn("theNextRecord");
		when(aReferencedRecord.getId()).thenReturn("aReferencedRecord");
		when(anotherReferencedRecord.getId()).thenReturn("anotherReferencedRecord");
		when(aThirdReferencedRecord.getId()).thenReturn("aThirdReferencedRecord");

		options = options.withNumberOfThreads(2).withQueueSize(6).withRecordsPerBatch(1);

	}

	@Test
	public void givenARecordServiceRuntimeExceptionOccurWhileExecutingTheSecondTransactionThenCloseThreadsAndThrowException()
			throws Exception {

		ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);

		doAnswer(workUntilExceptionTriggered()).doAnswer(triggerException(new RecordServicesRuntimeException("")))
				.when(recordServices).execute(any(Transaction.class));

		handler = new BulkRecordTransactionHandler(recordServices, "BulkRecordTransactionHandlerAcceptTest-test", options);

		handler.append(asList(aRecord), asList(aReferencedRecord));
		handler.append(asList(theRecordThrowingAnException), asList(anotherReferencedRecord, aThirdReferencedRecord));
		handler.append(asList(theNextRecord));

		triggerAnExceptionInAThread();
		Thread.sleep(100);

		try {
			handler.closeAndJoin();
			fail("Exception expected");
		} catch (BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction e) {
			//OK
		}

		verify(recordServices, times(2)).execute(transactionArgumentCaptor.capture());

		Transaction firstTransaction = transactionArgumentCaptor.getAllValues().get(0);
		Transaction secondTransaction = transactionArgumentCaptor.getAllValues().get(1);

		Transaction transactionWithARecord, transactionWithTheNextRecord;
		if (firstTransaction.getRecords().contains(aRecord)) {
			transactionWithARecord = firstTransaction;
			transactionWithTheNextRecord = secondTransaction;

		} else {
			transactionWithARecord = secondTransaction;
			transactionWithTheNextRecord = firstTransaction;
		}

		assertThat(transactionWithARecord.getRecords()).containsOnly(aRecord);
		assertThat(transactionWithARecord.getReferencedRecords()).hasSize(1).containsValue(aReferencedRecord);
		assertThat(transactionWithTheNextRecord.getRecords()).containsOnly(theRecordThrowingAnException);
		assertThat(transactionWithTheNextRecord.getReferencedRecords()).hasSize(2).containsValue(anotherReferencedRecord)
				.containsValue(aThirdReferencedRecord);

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
