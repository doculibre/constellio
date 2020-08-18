package com.constellio.data.dao.services.transactionLog.replay;

import com.constellio.data.dao.dto.sql.RecordTransactionSqlDTO;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransactionCombinator;
import com.constellio.data.dao.services.transactionLog.TransactionLogSqlReadWriteServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SqlTransactionLogReplayServices implements TransactionLogReplay<RecordTransactionSqlDTO> {

	public static int PARALLELISM = 8;

	public static int MAX_TRANSACTION_SIZE = BigVaultServerTransactionCombinator.DEFAULT_MAX_TRANSACTION_SIZE;

	private static final Logger LOGGER = LoggerFactory.getLogger(SqlTransactionLogReplayServices.class);

	private TransactionLogSqlReadWriteServices readWriteServices;

	private BigVaultServer bigVaultServer;

	private DataLayerLogger dataLayerLogger;

	public SqlTransactionLogReplayServices(
			TransactionLogSqlReadWriteServices readWriteServices,
			BigVaultServer bigVaultServer, DataLayerLogger dataLayerLogger) {
		this.readWriteServices = readWriteServices;
		this.bigVaultServer = bigVaultServer;
		this.dataLayerLogger = dataLayerLogger;

	}

	private void replayTransactionLog(BigVaultServerTransaction transaction,
									  BigVaultLogAddUpdater addUpdater) {
		addUpdater.add(transaction);
	}

	public void replayTransactionLogs(List<RecordTransactionSqlDTO> records) {
		TransactionsLogImportHandler transactionsLogImportHandler = new TransactionsLogImportHandler(
				bigVaultServer, dataLayerLogger, PARALLELISM);
		transactionsLogImportHandler.start();
		BigVaultLogAddUpdater addUpdater = new BigVaultLogAddUpdater(transactionsLogImportHandler);
		TransactionLogSqlReadWriteServices readerFactory = readWriteServices;
		for (RecordTransactionSqlDTO record : records) {
			LOGGER.info("Replaying record '" + record.getRecordId() + "'");
			replayTransactionLog(readerFactory.newOperation(record), addUpdater);
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
