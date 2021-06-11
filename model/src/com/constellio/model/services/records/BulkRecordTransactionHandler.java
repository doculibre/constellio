package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.threads.ConstellioThread;
import com.constellio.data.utils.ThreadList;
import com.constellio.data.utils.systemLogger.SystemLogger;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.BulkRecordTransactionHandlerRuntimeException.BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction;
import com.constellio.model.services.records.BulkRecordTransactionHandlerRuntimeException.BulkRecordTransactionHandlerRuntimeException_Interrupted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static com.constellio.model.entities.records.ImpactHandlingMode.HANDLE_ALL_IN_TRANSACTION_OR_EXCEPTION;
import static com.constellio.model.entities.records.ImpactHandlingMode.HANDLE_LATER;
import static com.constellio.model.entities.records.ImpactHandlingMode.NEXT_SYSTEM_REINDEXING;

public class BulkRecordTransactionHandler implements Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(BulkRecordTransactionHandler.class);

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

	int maxQueueSize = 0;
	LinkedBlockingQueue<BulkRecordTransactionHandlerTask> tasks;

	ThreadList<Thread> threadList;

	int currentRecordsEstimatedSize = 0;
	List<Record> currentRecords = new ArrayList<>();

	Map<String, Record> currentReferencedRecords = new HashMap<>();

	volatile Exception exception;

	String resourceName;

	boolean closed = false;

	List<Consumer<Transaction>> completedTransactionCallBacks = new ArrayList<>();

	public BulkRecordTransactionHandler(RecordServices recordServices, String resourceName) {
		this(recordServices, resourceName, new BulkRecordTransactionHandlerOptions());
	}

	public BulkRecordTransactionHandler(RecordServices recordServices, String resourceName,
										BulkRecordTransactionHandlerOptions options) {
		this.recordServices = recordServices;
		this.options = options;
		this.resourceName = resourceName;
		this.id = "" + sequence.incrementAndGet();
		maxQueueSize = options.queueSize;
		tasks = new LinkedBlockingQueue<>(options.queueSize);
		try {
			this.threadList = createThreadsAndStartThem();
		} catch (InterruptedException e) {
			throw new BulkRecordTransactionHandlerRuntimeException_Interrupted(e);
		}

	}

	public BulkRecordTransactionHandler registerCompletedTransactionCallBack(
			Consumer<Transaction> completedRecordCallBack) {
		this.completedTransactionCallBacks.add(completedRecordCallBack);
		return this;
	}

	public void unregisterCompletedTransactionCallBacks() {
		this.completedTransactionCallBacks.clear();
	}

	public synchronized void append(Record record) {
		List<Record> records = Collections.singletonList(record);
		append(records);
	}

	public synchronized void append(Record record, List<Record> referencedRecords) {
		List<Record> records = Collections.singletonList(record);
		append(records, referencedRecords);
	}

	public synchronized void append(List<Record> records) {
		append(records, new ArrayList<Record>());
	}

	public synchronized void append(List<Record> records, List<Record> referencedRecords) {
		ensureNoExceptions();

		int receivedEstimatedSize = estimatedSizeOf(records);

		if (currentRecords.size() + records.size() > options.recordsPerBatch) {
			pushCurrent();
		} else if (options.maxRecordsTotalSizePerBatch >= 0 &&
				   currentRecordsEstimatedSize + receivedEstimatedSize > options.maxRecordsTotalSizePerBatch) {
			pushCurrent();
		}

		total.addAndGet(records.size());
		currentRecords.addAll(records);
		currentRecordsEstimatedSize += receivedEstimatedSize;
		for (Record referencedRecord : referencedRecords) {
			currentReferencedRecords.put(referencedRecord.getId(), referencedRecord);
		}
	}

	private int estimatedSizeOf(List<Record> records) {
		int totalEstimatedSize = 0;

		for (Record record : records) {
			Integer estimatedSize = record.get(Schemas.ESTIMATED_SIZE);
			if (estimatedSize == null) {
				try {
					estimatedSize = RecordUtils.estimateRecordSize(record);
				} catch (Throwable t) {
					t.printStackTrace();
					estimatedSize = 50_000;
				}

			}
			totalEstimatedSize += estimatedSize;
		}

		return totalEstimatedSize;
	}

	public void pushCurrent() {

		long size = 0;
		for (Record record : currentRecords) {
			size += RecordUtils.estimateRecordSize(record);
		}

		if (!currentRecords.isEmpty()) {
			try {
				createdTasksCounter.incrementAndGet();
				if (options.isShowProgressionInConsole()) {
					LOGGER.info("Appending a task of " + currentRecords.size() + " records (" + size + " bytes)");
				}
				tasks.put(new BulkRecordTransactionHandlerTask(currentRecords, currentReferencedRecords));
			} catch (InterruptedException e) {
				throw new BulkRecordTransactionHandlerRuntimeException_Interrupted(e);
			}
			currentRecords = new ArrayList<>();
			currentRecordsEstimatedSize = 0;
			currentReferencedRecords = new HashMap<>();
		}
	}

	public void resetException() {
		this.exception = null;
	}

	public void closeAndJoin() {
		try {
			ensureNoExceptions();
			pushCurrent();
		} catch (BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction e) {
			currentRecords.clear();
			currentRecordsEstimatedSize = 0;
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

			closed = true;
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
						while (true) {
							try {

								BulkRecordTransactionHandlerTask task = tasks.poll(0, TimeUnit.SECONDS);
								if (task == NO_MORE_TASKS) {
									return;
								} else if (task != null) {
									availableWorkers.decrementAndGet();
									handle(task);
									availableWorkers.incrementAndGet();
									completedTasksCounter.incrementAndGet();

								} else {
									Thread.sleep(20);
								}

							} catch (InterruptedException e) {
								if (!options.isContinueOnExceptions()) {
									exception = e;
								}
								e.printStackTrace();
							}
						}

					} catch (Throwable t) {
						t.printStackTrace();
					}
					LOGGER.info("Thread " + Thread.currentThread().getName() + " has ended");
				}

				private void handle(BulkRecordTransactionHandlerTask task) {
					try {
						Transaction transaction = new Transaction(task.records);
						for (Record referencedRecord : task.referencedRecords.values()) {
							transaction.addReferencedRecord(referencedRecord);
						}
						transaction.setOptions(new RecordUpdateOptions(options.transactionOptions));

						RecordsFlushing flushing = RecordsFlushing.WITHIN_MINUTES(5);

						transaction.setRecordFlushing(flushing);
						transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);

						switch (options.recordModificationImpactHandling) {

							case IN_SAME_TRANSACTION:
								transaction.getRecordUpdateOptions().setImpactHandlingMode(HANDLE_ALL_IN_TRANSACTION_OR_EXCEPTION);
								recordServices.execute(transaction);
								break;
							case START_BATCH_PROCESS:
								transaction.getRecordUpdateOptions().setImpactHandlingMode(HANDLE_LATER);
								recordServices.execute(transaction);
								break;
							case NO_IMPACT_HANDLING:
								transaction.getRecordUpdateOptions().setImpactHandlingMode(NEXT_SYSTEM_REINDEXING);
								recordServices.execute(transaction);
								break;
						}

						completedTransactionCallBacks.forEach(callback -> callback.accept(transaction));

					} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_ExceptionWhileCalculating e) {
						SystemLogger.error(e.getMessage());
					} catch (Exception e) {
						if (!options.isContinueOnExceptions()) {
							exception = e;
						}
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
			LOGGER.info("Progression > " + progression.get() + " / " + total.get());
		}
	}

	private void ensureNoExceptions() {
		if (exception != null) {
			throw new BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction(exception);
		}
	}

	public void barrier() throws InterruptedException {
		pushCurrent();
		while (!isQueueEmptyAndWorkersWaiting()) {
			if (closed) {
				throw new InterruptedException("BulkRecordTransactionHandler was closed");
			}
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

	@Override
	public void close() throws IOException {
		closeAndJoin();
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

	public int getMaximumCountOfRecords() {
		return (options.getNumberOfThreads() * options.getRecordsPerBatch())
			   + (options.getQueueSize() * options.getRecordsPerBatch());
	}

	public int getNumberOfThreads() {
		return options.getNumberOfThreads();
	}

	public int getRecordsPerBatch() {
		return options.getRecordsPerBatch();
	}

	public int getMaxQueueSize() {
		return maxQueueSize;
	}
}