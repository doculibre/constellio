package com.constellio.data.dao.services.transactionLog.replay;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.utils.ThreadList;

public class TransactionsLogImportHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsLogImportHandler.class);

	private DataLayerLogger dataLayerLogger;
	private LinkedBlockingQueue<ReplayedTransactionQueueItem> transactions;
	private AtomicInteger executedTransactions = new AtomicInteger();
	private ThreadList<TransactionsLogImportHandlerThread> threads = new ThreadList<>();
	private int parallelism;
	private Semaphore availableThreads;
	private BigVaultServer bigVaultServer;

	public TransactionsLogImportHandler(BigVaultServer bigVaultServer, DataLayerLogger dataLayerLogger, int parallelism) {
		this.dataLayerLogger = dataLayerLogger;
		this.bigVaultServer = bigVaultServer;
		this.parallelism = parallelism;
	}

	public void start() {
		transactions = new LinkedBlockingQueue<>(parallelism);
		for (int i = 0; i < parallelism; i++) {
			threads.addAndStart(new TransactionsLogImportHandlerThread());
		}
		availableThreads = new Semaphore(parallelism);
	}

	public void join() {
		for (int i = 0; i < parallelism; i++) {
			try {
				transactions.put(new ReplayedTransactionQueueItem(null));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			threads.joinAll();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		try {
			bigVaultServer.softCommit();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
	}

	public void pushTransaction(BigVaultServerTransaction transaction) {
		try {
			transactions.put(new ReplayedTransactionQueueItem(transaction));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private class TransactionsLogImportHandlerThread extends Thread {
		@Override
		public void run() {

			while (true) {
				BigVaultServerTransaction transaction;
				try {

					synchronized (TransactionsLogImportHandler.this) {

						try {
							transaction = transactions.take().transaction;
							transaction.setRecordsFlushing(RecordsFlushing.LATER());
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}

						if (transaction != null) {
							if (!transaction.isParallelisable()) {
								availableThreads.acquire(parallelism);
								availableThreads.release(parallelism);
							}

							availableThreads.acquire();

						}
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);

				}

				if (transaction == null) {
					break;
				} else {
					try {
						dataLayerLogger.logTransaction(transaction);
						int executedTransactionsCount = executedTransactions.incrementAndGet();
						if (executedTransactionsCount > 9) {
							LOGGER.info("Replaying transactions - write #" + (++executedTransactionsCount));
						}
						bigVaultServer.addAll(transaction);
					} catch (BigVaultException e) {
						throw new RuntimeException(e);
					} finally {
						availableThreads.release();
					}
				}
			}

		}
	}

	private static class ReplayedTransactionQueueItem {

		BigVaultServerTransaction transaction;

		private ReplayedTransactionQueueItem(BigVaultServerTransaction transaction) {
			this.transaction = transaction;
		}
	}
}
