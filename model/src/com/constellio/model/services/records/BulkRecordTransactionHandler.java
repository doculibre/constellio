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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.threads.ConstellioThread;
import com.constellio.data.utils.ThreadList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.BulkRecordTransactionHandlerRuntimeException.BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction;
import com.constellio.model.services.records.BulkRecordTransactionHandlerRuntimeException.BulkRecordTransactionHandlerRuntimeException_Interrupted;

public class BulkRecordTransactionHandler {

	AtomicLong sequence = new AtomicLong();

	String id;

	static final BulkRecordTransactionHandlerTask NO_MORE_TASKS = new BulkRecordTransactionHandlerTask(null, null);

	AtomicInteger createdTasksCounter = new AtomicInteger();

	AtomicInteger completedTasksCounter = new AtomicInteger();

	AtomicInteger availableWorkers = new AtomicInteger();

	AtomicInteger progression = new AtomicInteger();

	AtomicInteger total = new AtomicInteger();

	BulkRecordTransactionHandlerOptions options;

	RecordServices recordServices;

	LinkedBlockingQueue<BulkRecordTransactionHandlerTask> tasks;

	ThreadList<Thread> threadList;

	List<Record> currentRecords = new ArrayList<>();

	Map<String, Record> currentReferencedRecords = new HashMap<>();

	Exception exception;

	String resourceName;

	public BulkRecordTransactionHandler(RecordServices recordServices, String resourceName) {
		this(recordServices, resourceName, new BulkRecordTransactionHandlerOptions());
	}

	public BulkRecordTransactionHandler(RecordServices recordServices, String resourceName,
			BulkRecordTransactionHandlerOptions options) {
		this.recordServices = recordServices;
		this.options = options;
		this.resourceName = resourceName;
		this.id = "" + sequence.incrementAndGet();
		tasks = new LinkedBlockingQueue<>(options.queueSize);
		try {
			this.threadList = createThreadsAndStartThem();
		} catch (InterruptedException e) {
			throw new BulkRecordTransactionHandlerRuntimeException_Interrupted(e);
		}

	}

	public synchronized void append(Record record) {
		List<Record> records = Collections.singletonList(record);
		append(records);
	}

	public synchronized void append(List<Record> records) {
		append(records, new ArrayList<Record>());
	}

	public synchronized void append(List<Record> records, List<Record> referencedRecords) {
		ensureNoExceptions();
		if (currentRecords.size() + records.size() > options.recordsPerBatch) {
			pushCurrent();
		}
		total.addAndGet(records.size());
		currentRecords.addAll(records);
		for (Record referencedRecord : referencedRecords) {
			currentReferencedRecords.put(referencedRecord.getId(), referencedRecord);
		}
	}

	public void pushCurrent() {

		if (!currentRecords.isEmpty()) {
			try {
				createdTasksCounter.incrementAndGet();
				tasks.put(new BulkRecordTransactionHandlerTask(currentRecords, currentReferencedRecords));
			} catch (InterruptedException e) {
				throw new BulkRecordTransactionHandlerRuntimeException_Interrupted(e);
			}
			currentRecords = new ArrayList<>();
			currentReferencedRecords = new HashMap<>();
		}
	}

	public void closeAndJoin() {
		try {
			ensureNoExceptions();
			pushCurrent();
		} catch (BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction e) {
			currentRecords.clear();
			tasks.clear();
			throw e;
		} finally {

			for (int i = 0; i < options.numberOfThreads; i++) {
				try {
					tasks.put(NO_MORE_TASKS);
				} catch (InterruptedException e) {
					throw new BulkRecordTransactionHandlerRuntimeException_Interrupted(e);
				}
			}

			try {
				threadList.joinAll();
			} catch (InterruptedException e) {
				throw new BulkRecordTransactionHandlerRuntimeException_Interrupted(e);
			}

		}
		recordServices.flush();
		ensureNoExceptions();
	}

	ThreadList<Thread> createThreadsAndStartThem()
			throws InterruptedException {
		ThreadList<Thread> threads = new ThreadList<>();

		for (int i = 0; i < options.numberOfThreads; i++) {
			availableWorkers.incrementAndGet();
			String threadId = "BulkRecordTransactionHandler-" + resourceName + "-" + id + "-" + i;
			threads.add(new ConstellioThread(threadId) {
				@Override
				public void execute() {
					try {
						while (exception == null) {
							try {

								BulkRecordTransactionHandlerTask task = tasks.poll(0, TimeUnit.SECONDS);
								if (task == NO_MORE_TASKS) {
									return;
								} else if (task != null) {
									availableWorkers.decrementAndGet();
									handle(task);
									availableWorkers.incrementAndGet();
									completedTasksCounter.incrementAndGet();

								}

							} catch (InterruptedException e) {
								exception = e;
								e.printStackTrace();
							}
						}

					} catch (Throwable t) {
						t.printStackTrace();
					}
					System.out.println("Thread " + Thread.currentThread().getName() + " has ended");
				}

				private void handle(BulkRecordTransactionHandlerTask task) {
					try {
						Transaction transaction = new Transaction(task.records);
						for (Record referencedRecord : task.referencedRecords.values()) {
							transaction.addReferencedRecord(referencedRecord);
						}
						transaction.setOptions(new RecordUpdateOptions(options.transactionOptions));
						transaction.setRecordFlushing(RecordsFlushing.LATER());
						transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);

						switch (options.recordModificationImpactHandling) {

						case IN_SAME_TRANSACTION:
							recordServices.execute(transaction);
							break;
						case START_BATCH_PROCESS:
							recordServices.executeHandlingImpactsAsync(transaction);
							break;
						case NO_IMPACT_HANDLING:
							recordServices.executeWithImpactHandler(transaction, null);
							break;
						}
					} catch (Exception e) {
						exception = e;
						e.printStackTrace();

					}
					if (task != null) {
						progression.addAndGet(task.records.size());
					}
					logProgression();
				}
			});
		}

		threads.startAll();

		return threads;
	}

	void logProgression() {
		if (options.showProgressionInConsole) {
			System.out.println("Progression > " + progression.get() + " / " + total.get());
		}
	}

	private void ensureNoExceptions() {
		if (exception != null) {
			throw new BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction(exception);
		}
	}

	public void barrier() {

		while (!isQueueEmptyAndWorkersWaiting()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private boolean isQueueEmptyAndWorkersWaiting() {
		return tasks.isEmpty() && availableWorkers.get() == threadList.size()
				&& createdTasksCounter.get() == completedTasksCounter.get();
	}

	static class BulkRecordTransactionHandlerTask {

		private List<Record> records;

		private Map<String, Record> referencedRecords;

		BulkRecordTransactionHandlerTask(List<Record> records,
				Map<String, Record> referencedRecords) {
			this.referencedRecords = referencedRecords;
			this.records = records;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof BulkRecordTransactionHandlerTask)) {
				return false;
			}

			BulkRecordTransactionHandlerTask task = (BulkRecordTransactionHandlerTask) o;

			if (records != null ? !records.equals(task.records) : task.records != null) {
				return false;
			}
			if (referencedRecords != null ? !referencedRecords.equals(task.referencedRecords) : task.referencedRecords != null) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = records != null ? records.hashCode() : 0;
			result = 31 * result + (referencedRecords != null ? referencedRecords.hashCode() : 0);
			return result;
		}
	}
}
