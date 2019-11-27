package com.constellio.data.dao.services.replicationFactor;

import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.replicationFactor.dto.ReplicationFactorSolrInputField;
import com.constellio.data.dao.services.replicationFactor.dto.ReplicationFactorTransaction;
import com.constellio.data.dao.services.replicationFactor.dto.ReplicationFactorTransactionType;
import com.constellio.data.utils.TimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.constellio.data.dao.services.replicationFactor.utils.ReplicationFactorTransactionParser.toJson;

@Slf4j
public class ReplicationFactorTransactionWriteService {

	private BlockingQueue<ReplicationFactorTransaction> replicationFactorTransactions;
	private ReplicationFactorLogService replicationFactorLogService;

	private ScheduledExecutorService executor;
	private ReplicationFactorTransactionWriteTask task;
	private Future taskFuture;

	ReplicationFactorTransactionWriteService(ReplicationFactorLogService replicationFactorLogService) {
		replicationFactorTransactions = new LinkedBlockingQueue<>();
		this.replicationFactorLogService = replicationFactorLogService;

		executor = Executors.newSingleThreadScheduledExecutor();
		task = new ReplicationFactorTransactionWriteTask();
		taskFuture = executor.submit(task);
	}

	public void add(BigVaultServerTransaction transaction, TransactionResponseDTO responseDTO) {
		addDocuments(transaction.getNewDocuments(), ReplicationFactorTransactionType.ADD);
		addDocuments(transaction.getUpdatedDocuments(), ReplicationFactorTransactionType.UPDATE);
		addDeleteDocuments(transaction.getDeletedRecords());
	}

	private void addDocuments(List<SolrInputDocument> documents, ReplicationFactorTransactionType type) {
		for (SolrInputDocument updateDocument : documents) {
			if (isNotRecordTransaction(updateDocument)) {
				continue;
			}

			String id = (String) updateDocument.getField("id").getValue();
			String version = String.valueOf(updateDocument.getField("_version_").getValue());
			replicationFactorTransactions.add(
					ReplicationFactorTransaction.builder()
							.id(UUID.randomUUID().toString())
							.recordId(id)
							.type(type)
							.version(version != null ? Long.valueOf(version) : -1)
							.timestamp(epoch(TimeProvider.getLocalDateTime()))
							.fields(toSolrInputFields(SolrUtils.getFields(updateDocument)))
							.build());

			log.info("Detected " + type.name() + " for record " + id + " version " + version);
		}
	}

	private void addDeleteDocuments(List<String> documentIds) {
		for (String id : documentIds) {
			replicationFactorTransactions.add(
					ReplicationFactorTransaction.builder()
							.id(UUIDV1Generator.newRandomId())
							.recordId(id)
							.type(ReplicationFactorTransactionType.DELETE)
							.version(Long.MAX_VALUE)
							.timestamp(epoch(TimeProvider.getLocalDateTime()))
							.build());

			log.info("Detected DELETE for record " + id);
		}
	}

	public void stop() {
		if (executor != null) {
			task.cancel();
			taskFuture.cancel(true);
			executor.shutdown();
			try {
				if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
			}
		}
	}

	private Map<String, ReplicationFactorSolrInputField> toSolrInputFields(Map<String, SolrInputField> fields) {
		Map<String, ReplicationFactorSolrInputField> convertedFields = new HashMap<>();
		for (Map.Entry<String, SolrInputField> entry : fields.entrySet()) {
			ReplicationFactorSolrInputField convertedField = new ReplicationFactorSolrInputField(entry.getValue().getName(), entry.getValue().getValue());
			convertedFields.put(entry.getKey(), convertedField);
		}
		return convertedFields;
	}

	private boolean isNotRecordTransaction(SolrInputDocument solrDocument) {
		SolrInputField schemaField = solrDocument.getField("schema_s");
		if (schemaField != null &&
			(((String) schemaField.getValue()).startsWith("event_") ||
			 ((String) schemaField.getValue()).startsWith("temporaryRecord_"))) {
			return true;
		}

		SolrInputField reindexField = solrDocument.getField("markedForReindexing_s");
		if (reindexField != null) {
			if (reindexField.getValue() != null && reindexField.getValue() instanceof HashMap) {
				HashMap<String, String> reindexValue = (HashMap<String, String>) reindexField.getValue();
				if (reindexValue != null && reindexValue.containsKey("set")) {
					return reindexValue.get("set") != null && reindexValue.get("set").equals("__TRUE__");
				}
			}
		}
		return false;
	}

	private long epoch(LocalDateTime localDateTime) {
		DateTime utc = localDateTime.toDateTime(DateTimeZone.UTC);
		return utc.getMillis();
	}

	class ReplicationFactorTransactionWriteTask implements Runnable {

		volatile boolean cancel = false;

		@Override
		public void run() {

			while (!cancel) {
				try {
					ReplicationFactorTransaction transaction = replicationFactorTransactions.take();

					try {
						writeToLog(transaction);
					} catch (Exception e) {
						log.error(String.format("Failed to save version %d for record %s to transactional log",
								transaction.getVersion(), transaction.getRecordId()), e);
						replicationFactorTransactions.put(transaction);
					}
				} catch (Exception ignored) {
				}
			}

		}

		public void cancel() {
			cancel = true;
		}

		private void writeToLog(ReplicationFactorTransaction transaction) throws IOException {
			replicationFactorLogService.writeLineToLocalLog(toJson(transaction));

			log.info(String.format("Version %d for record %s saved to transactional log",
					transaction.getVersion(), transaction.getRecordId()));
		}
	}

}
