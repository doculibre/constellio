package com.constellio.data.dao.services.transactionLog.kafka;

import static com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransactionCombinator.DEFAULT_MAX_TRANSACTION_SIZE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransactionCombinator;
import com.constellio.data.dao.services.transactionLog.reader1.ReaderTransactionLinesIteratorV1;
import com.constellio.data.dao.services.transactionLog.reader1.ReaderTransactionsIteratorV1;
import com.constellio.data.dao.services.transactionLog.replay.TransactionsLogImportHandler;

public class TransactionReplayer implements ConsumerRecordCallback<String, Transaction> {
	private final DataLayerConfiguration configuration;
	private final BigVaultLogAddUpdater addUpdater;
	private Map<Long, String> transactions;
	private String[] requestDelimiters = { "addUpdate ", "delete ", "deletequery ", "sequence next ", "sequence set " };

	public TransactionReplayer(DataLayerConfiguration configuration, BigVaultServer bigVaultServer,
			DataLayerLogger dataLayerLogger) {
		this.configuration = configuration;

		TransactionsLogImportHandler transactionsLogImportHandler = new TransactionsLogImportHandler(bigVaultServer,
				dataLayerLogger, 2);
		transactionsLogImportHandler.start();

		addUpdater = new BigVaultLogAddUpdater(transactionsLogImportHandler);
		
		setTransactions(new TreeMap<Long, String>());
	}

	@Override
	public void onConsumerRecord(long offset, String topic, String key, Transaction value) {
		String t = StringUtils.remove(value.getTransaction(), "--transaction--");
		
		List<String> list = new ArrayList<>();
		String[] split = t.split("\n");
		for (int i = 0; i < split.length; i++) {

			if (StringUtils.containsAny(split[i], requestDelimiters)) {
				insertTransaction(list, value);
				list.clear();
			}

			list.add(split[i]);
		}
		
		insertTransaction(list, value);
	}

	private void insertTransaction(List<String> list, Transaction transaction) {
		if (!list.isEmpty()) {
			String request = StringUtils.join(list, "\n");

			if (StringUtils.trimToNull(request) != null) {
				Long version = null;
				
				Set<Entry<String, Long>> entrySet = transaction.getVersions().entrySet();
				for (Entry<String, Long> entry : entrySet) {
					if(contains(list.get(0), entry.getKey())) {
						version = entry.getValue();
					}
				}
				
				transactions.put(version, request);
			}
		}
	}

	private boolean contains(String line, String key) {
		String[] split = line.split("\\s+");
		for (int i = 0; i < split.length; i++) {
			if(split[i].equals(key)) {
				return true;
			}
		}
		return false;
	}

	protected void replayTransactionLog(String value) {
		Iterator<BigVaultServerTransaction> iteratorTransaction = toIteratorTransaction(value);
		while (iteratorTransaction.hasNext()) {
			addUpdater.add(iteratorTransaction.next());
		}
	}

	protected Iterator<BigVaultServerTransaction> toIteratorTransaction(String transaction) {
		List<String> lines = Arrays.asList(StringUtils.split(transaction, "\n"));
		Iterator<List<String>> transactionLinesIterator = new ReaderTransactionLinesIteratorV1(lines.iterator());
		return new ReaderTransactionsIteratorV1("Kafka", transactionLinesIterator, configuration);
	}

	public void replayAllAndClose() {
		Set<Entry<Long, String>> entrySet = transactions.entrySet();
		for (Entry<Long, String> transaction : entrySet) {
			replayTransactionLog(transaction.getValue());
		}

		addUpdater.close();
		transactions.clear();
	}

	private static class BigVaultLogAddUpdater {

		TransactionsLogImportHandler replayHandler;
		BigVaultServerTransactionCombinator combinator;

		private BigVaultLogAddUpdater(TransactionsLogImportHandler replayHandler) {
			this.replayHandler = replayHandler;
			this.combinator = new BigVaultServerTransactionCombinator(DEFAULT_MAX_TRANSACTION_SIZE);
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

			replayHandler.join();
		}
	}

	public void setTransactions(Map<Long, String> transactions) {
		this.transactions = transactions;
	}
}
