package com.constellio.data.dao.services.transactionLog.replay;

import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransactionCombinator;
import com.constellio.data.dao.services.transactionLog.TransactionLogReadWriteServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class TransactionLogReplayServices implements TransactionLogReplay<File> {

	public static int PARALLELISM = 8;

	public static int MAX_TRANSACTION_SIZE = BigVaultServerTransactionCombinator.DEFAULT_MAX_TRANSACTION_SIZE;

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionLogReplayServices.class);

	private TransactionLogReadWriteServices readWriteServices;

	private BigVaultServer bigVaultServer;

	private DataLayerLogger dataLayerLogger;

	public TransactionLogReplayServices(
			TransactionLogReadWriteServices readWriteServices,
			BigVaultServer bigVaultServer, DataLayerLogger dataLayerLogger) {
		this.readWriteServices = readWriteServices;
		this.bigVaultServer = bigVaultServer;
		this.dataLayerLogger = dataLayerLogger;

	}

	private void replayTransactionLog(Iterator<BigVaultServerTransaction> transactionIterator,
									  BigVaultLogAddUpdater addUpdater) {
		while (transactionIterator.hasNext()) {
			addUpdater.add(transactionIterator.next());
		}

	}

	@Override
	public void replayTransactionLogs(List<File> tLogs) {
		TransactionsLogImportHandler transactionsLogImportHandler = new TransactionsLogImportHandler(
				bigVaultServer, dataLayerLogger, PARALLELISM);
		transactionsLogImportHandler.start();
		BigVaultLogAddUpdater addUpdater = new BigVaultLogAddUpdater(transactionsLogImportHandler);
		TransactionLogReadWriteServices readerFactory = readWriteServices;
		for (File tLog : tLogs) {
			LOGGER.info("Replaying tlog '" + tLog.getName() + "'");
			replayTransactionLog(readerFactory.newOperationsIterator(tLog), addUpdater);
		}
		addUpdater.close();
		transactionsLogImportHandler.join();
	}

	private static class BigVaultLogAddUpdater {

		TransactionsLogImportHandler replayHandler;
		BigVaultServerTransactionCombinator combinator;

		private BigVaultLogAddUpdater(TransactionsLogImportHandler replayHandler) {
			this.replayHandler = replayHandler;
			this.combinator = new BigVaultServerTransactionCombinator(MAX_TRANSACTION_SIZE);
		}

		private void add(BigVaultServerTransaction newTransaction) {
			if (combinator.canCombineWith(newTransaction)) {
				combinator.combineWith(newTransaction);

			} else {
				replayHandler.pushTransaction(combinator.combineAndClean());
				combinator.combineWith(newTransaction);
			}
		}

		public void close() {

			if (combinator.hasData()) {
				replayHandler.pushTransaction(combinator.combineAndClean());
			}
		}
	}

}
