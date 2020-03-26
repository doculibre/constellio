package com.constellio.data.dao.services.replicationFactor;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.bigVault.BigVaultRecordDao;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.SolrCloudUtils;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.leaderElection.LeaderElectionManagerObserver;
import com.constellio.data.dao.services.replicationFactor.dto.ReplicationFactorSolrInputField;
import com.constellio.data.dao.services.replicationFactor.dto.ReplicationFactorTransaction;
import com.constellio.data.dao.services.replicationFactor.dto.ReplicationFactorTransactionType;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.extensions.ReplicationFactorManagerExtension.TransactionReplayed;
import com.mysql.jdbc.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static com.constellio.data.dao.services.replicationFactor.utils.ReplicationFactorTransactionParser.toTransaction;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class ReplicationFactorTransactionReadService implements LeaderElectionManagerObserver {

	private ReplicationFactorLogService replicationFactorLogService;

	private DataLayerFactory dataLayerFactory;
	private DataLayerSystemExtensions extensions;

	ScheduledExecutorService executor;
	private ScheduledFuture scheduledFuture;
	private Map<String, SortedSet<ReplicationFactorTransaction>> transactionsByRecordIds;

	private final static String VERSION_FIELD = "_version_";

	ReplicationFactorTransactionReadService(DataLayerFactory dataLayerFactory,
											ReplicationFactorLogService replicationFactorLogService,
											DataLayerSystemExtensions extensions) {
		transactionsByRecordIds = new LinkedHashMap<>();

		this.dataLayerFactory = dataLayerFactory;
		this.replicationFactorLogService = replicationFactorLogService;
		this.extensions = extensions;
	}

	public void start() {
		// FIXME Disabled for now, will be activated once we fully support solr 8+
		/*
		if (dataLayerFactory.getLeaderElectionService().isCurrentNodeLeader()) {
			executor = Executors.newSingleThreadScheduledExecutor();
			scheduledFuture = executor.scheduleWithFixedDelay(
					createReplicationFactorReadTask(), 1, 1, SECONDS);
		}
		*/
	}

	Runnable createReplicationFactorReadTask() {
		return new ReplicationFactorTransactionReadTask();
	}

	public void stop() {
		if (executor != null) {
			scheduledFuture.cancel(false);
			executor.shutdown();
			try {
				if (!executor.awaitTermination(5, SECONDS)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
			}
		}
	}

	private void replayDegradedStateTransactions() {
		int lineCount = loadLogFile();
		if (lineCount == 0) {
			return;
		}

		Map<String, TransactionReplayed> replayedTransactionByRecordId = new HashMap<>();
		Set<String> replayedTransactionIds = new HashSet<>();
		for (String recordId : transactionsByRecordIds.keySet()) {
			long version = -1;
			try {
				version = getRecordDao().get(recordId).getVersion();
			} catch (NoSuchRecordWithId ignored) {
			}

			long currentVersion = version;
			for (ReplicationFactorTransaction transaction : getTransactionsToReplay(recordId, version, replayedTransactionIds)) {
				BigVaultServerTransaction bigVaultServerTransaction;

				if (transaction.getType() == ReplicationFactorTransactionType.ADD) {
					SolrInputDocument solrDocument = new SolrInputDocument(toSolrInputFields(transaction.getFields()));
					bigVaultServerTransaction = new BigVaultServerTransaction(RecordsFlushing.NOW(),
							singletonList(solrDocument), Collections.<SolrInputDocument>emptyList(),
							Collections.<String>emptyList(), Collections.<String>emptyList());
				} else if (transaction.getType() == ReplicationFactorTransactionType.DELETE) {
					bigVaultServerTransaction = new BigVaultServerTransaction(RecordsFlushing.NOW(),
							Collections.<SolrInputDocument>emptyList(), Collections.<SolrInputDocument>emptyList(),
							singletonList(transaction.getRecordId()), Collections.<String>emptyList());
				} else {
					transaction.getFields().get(VERSION_FIELD).setValue(currentVersion);
					SolrInputDocument solrDocument = new SolrInputDocument(toSolrInputFields(transaction.getFields()));
					bigVaultServerTransaction = new BigVaultServerTransaction(RecordsFlushing.NOW(),
							Collections.<SolrInputDocument>emptyList(), singletonList(solrDocument),
							Collections.<String>emptyList(), Collections.<String>emptyList());
				}

				try {
					TransactionResponseDTO responseDTO = dataLayerFactory.getRecordsVaultServer().addAll(bigVaultServerTransaction);
					currentVersion = bigVaultServerTransaction.getDeletedRecords().isEmpty() ?
									 responseDTO.getNewDocumentVersion(recordId) : -1;
					replayedTransactionIds.add(transaction.getId());

					updateReplayedTransaction(replayedTransactionByRecordId, recordId, currentVersion);

					log.info("Restored version " + transaction.getVersion() + " for record id " + transaction.getRecordId());
				} catch (Exception e) {
					log.error("Failed to restore version " + transaction.getVersion() + " for record id " + transaction.getRecordId(), e);
					break;
				}
			}
		}

		if (!replayedTransactionByRecordId.isEmpty()) {
			extensions.onTransactionsReplayed(replayedTransactionByRecordId.values());
		}

		replicationFactorLogService.removeLinesFromMergedLog(lineCount, replayedTransactionIds);
	}

	private void updateReplayedTransaction(Map<String, TransactionReplayed> replayedTransactionByRecordId,
										   String recordId, long currentVersion) {
		Long version = currentVersion != -1 ? currentVersion : null;
		if (replayedTransactionByRecordId.containsKey(recordId)) {
			replayedTransactionByRecordId.get(recordId).setVersion(version);
		} else {
			replayedTransactionByRecordId.put(recordId, new TransactionReplayed(recordId, version));
		}
	}

	private Collection<ReplicationFactorTransaction> getTransactionsToReplay(String recordId, long version,
																			 Set<String> replayedTransactionIds) {
		Set<ReplicationFactorTransaction> allTransactions = transactionsByRecordIds.get(recordId);
		if (allTransactions.isEmpty()) {
			return allTransactions;
		}

		if (version == -1 && allTransactions.iterator().next().getType() != ReplicationFactorTransactionType.ADD) {
			for (ReplicationFactorTransaction transaction : allTransactions) {
				replayedTransactionIds.add(transaction.getId());
			}
			return Collections.emptyList();
		}

		List<ReplicationFactorTransaction> replayTransactions = new ArrayList<>();
		for (ReplicationFactorTransaction transaction : allTransactions) {
			if (transaction.getVersion() >= version) {
				replayTransactions.add(transaction);
			} else {
				// we add the ignored transactions so that they are removed from the log file
				replayedTransactionIds.add(transaction.getId());
			}
		}
		return replayTransactions;
	}

	private int loadLogFile() {
		try {
			transactionsByRecordIds.clear();

			File mergedLogFile = replicationFactorLogService.mergeAllLogFiles();

			Set<ReplicationFactorTransaction> sortedTransactions = new TreeSet<>(getComparator());

			int lineCount = 0;
			try (BufferedReader bufferedReader = new BufferedReader(new FileReader(mergedLogFile))) {
				String line;
				while (!StringUtils.isNullOrEmpty(line = bufferedReader.readLine())) {
					ReplicationFactorTransaction t = toTransaction(line);
					sortedTransactions.add(toTransaction(line));
					lineCount++;
				}
			}

			for (ReplicationFactorTransaction transaction : sortedTransactions) {
				if (transactionsByRecordIds.containsKey(transaction.getRecordId())) {
					transactionsByRecordIds.get(transaction.getRecordId()).add(transaction);
				} else {
					transactionsByRecordIds.put(transaction.getRecordId(), new TreeSet<>(singletonList(transaction)));
				}
			}

			return lineCount;
		} catch (FileNotFoundException e) {
			return 0;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, SolrInputField> toSolrInputFields(Map<String, ReplicationFactorSolrInputField> fields) {
		Map<String, SolrInputField> convertedFields = new HashMap<>();
		for (Map.Entry<String, ReplicationFactorSolrInputField> entry : fields.entrySet()) {
			SolrInputField convertedField = new SolrInputField(entry.getValue().getName());
			convertedField.setValue(entry.getValue().getValue());
			convertedFields.put(entry.getKey(), convertedField);
		}
		return convertedFields;
	}

	private BigVaultRecordDao getRecordDao() {
		return (BigVaultRecordDao) dataLayerFactory.newRecordDao();
	}

	Comparator<ReplicationFactorTransaction> getComparator() {
		return new Comparator<ReplicationFactorTransaction>() {
			@Override
			public int compare(ReplicationFactorTransaction o1, ReplicationFactorTransaction o2) {
				if (o1.getTimestamp() < o2.getTimestamp()) {
					return -1;
				} else if (o1.getTimestamp() > o2.getTimestamp()) {
					return 1;
				} else {
					if (o1.equals(o2)) {
						return 0;
					} else {
						return -1;
					}
				}
			}
		};
	}

	@Override
	public void onLeaderStatusChanged(boolean newStatus) {
		if (newStatus) {
			start();
		} else {
			stop();
		}
	}

	class ReplicationFactorTransactionReadTask implements Runnable {

		@Override
		public void run() {
			try {
				if (isSolrCloudOnline()) {
					replayDegradedStateTransactions();
				}
			} catch (Exception e) {
				log.error("Error encountered while trying to replay transactions", e);
			}
		}

		boolean isSolrCloudOnline() {
			return SolrCloudUtils.isOnline(dataLayerFactory.getRecordsVaultServer().getNestedSolrServer());
		}
	}

}
