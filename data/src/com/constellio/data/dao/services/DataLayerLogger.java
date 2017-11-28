package com.constellio.data.dao.services;

import static com.constellio.data.utils.LoggerUtils.toParamsString;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;

public class DataLayerLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataLayerLogger.class);

	private List<String> monitoredIds = new ArrayList<>();

	private boolean logFL = true;

	private int printAllQueriesLongerThanMS = 10000;

	private int slowQueryDuration = 20000;

	private int verySlowQueryDuration = 30000;

	public boolean logAllTransactions = false;

	private boolean queryLoggingEnabled = true;

	public void logQueryResponse(SolrParams params, QueryResponse response) {

		if (queryLoggingEnabled) {
			String prefix = null;
			if (response.getQTime() >= verySlowQueryDuration) {
				prefix = "VERY SLOW QUERY : ";

			} else if (response.getQTime() >= slowQueryDuration) {
				prefix = "SLOW QUERY : ";

			} else if (response.getQTime() >= printAllQueriesLongerThanMS) {
				prefix = "QUERY : ";
			}

			if (prefix != null) {
				if (!toParamsString(params).contains("markedForReindexing_s")) {
					LOGGER.info(prefix + "qtime=" + response.getQTime() + ", numfound=" + response.getResults().getNumFound()
							+ ", documents=" + response.getResults().size()
							+ "\n" + toParamsString(params, "qt", "shards.qt", logFL ? "" : "fl") + "\n");
				}
			}
		}
	}

	public void logTransaction(TransactionDTO transaction) {
		StringBuilder logBuilder = new StringBuilder();

		for (RecordDTO recordDTO : transaction.getNewRecords()) {
			if (logAllTransactions || monitoredIds.contains(recordDTO.getId())) {
				logBuilder.append("\n\t" + toString(recordDTO));
			}
		}

		for (RecordDeltaDTO recordDeltaDTO : transaction.getModifiedRecords()) {
			if (logAllTransactions || monitoredIds.contains(recordDeltaDTO.getId())) {
				logBuilder.append("\n\t" + toString(recordDeltaDTO));
			}
		}

		String log = logBuilder.toString();
		if (!log.isEmpty()) {
			LOGGER.info("Transaction #" + transaction.getTransactionId() + log);
		}
	}

	public void logTransaction(BigVaultServerTransaction transaction) {
		StringBuilder logBuilder = new StringBuilder();

		for (SolrInputDocument recordDTO : transaction.getNewDocuments()) {
			String id = (String) recordDTO.getFieldValue("id");
			if (logAllTransactions || monitoredIds.contains(id)) {
				logBuilder.append("\n\t" + toString(recordDTO));
			}
		}

		for (SolrInputDocument recordDTO : transaction.getUpdatedDocuments()) {
			String id = (String) recordDTO.getFieldValue("id");
			if (logAllTransactions || monitoredIds.contains(id)) {
				logBuilder.append("\n\t" + toString(recordDTO));
			}
		}

		String log = logBuilder.toString();
		if (!log.isEmpty()) {
			LOGGER.info("Transaction #" + transaction.getTransactionId() + log);
		}
	}

	private static List<String> hiddenFieldsInLogs = asList("modifiedOn_dt", "createdOn_dt");

	private String toString(RecordDeltaDTO recordDeltaDTO) {
		StringBuilder log = new StringBuilder();

		log.append(recordDeltaDTO.getId() + ">>  ");
		for (String modifiedField : recordDeltaDTO.getModifiedFields().keySet()) {
			if (!hiddenFieldsInLogs.contains(modifiedField)) {
				log.append(modifiedField);
				log.append(":");
				log.append(recordDeltaDTO.getInitialFields().get(modifiedField));
				log.append("=>");
				log.append(recordDeltaDTO.getModifiedFields().get(modifiedField));
				log.append("; ");
			}
		}

		return log.toString();
	}

	private String toString(RecordDTO recordDTO) {
		StringBuilder log = new StringBuilder();

		log.append(recordDTO.getId() + ">>  ");
		for (Map.Entry<String, Object> field : recordDTO.getFields().entrySet()) {
			if (!hiddenFieldsInLogs.contains(field.getKey())) {
				log.append(field.getKey());
				log.append(":");
				log.append(field.getValue());
				log.append("; ");
			}
		}
		for (Map.Entry<String, Object> field : recordDTO.getCopyFields().entrySet()) {
			if (!hiddenFieldsInLogs.contains(field.getKey())) {
				log.append(field.getKey());
				log.append(":");
				log.append(field.getValue());
				log.append("; ");
			}
		}

		return log.toString();
	}

	private String toString(SolrInputDocument document) {
		StringBuilder log = new StringBuilder();
		String id = (String) document.getFieldValue("id");
		log.append(id + ">>  ");
		for (String fieldName : document.getFieldNames()) {
			Object fieldValues = document.getFieldValues(fieldName);
			if (!hiddenFieldsInLogs.contains(fieldName)) {
				log.append(fieldName);
				log.append(":");
				log.append(fieldValues);
				log.append("; ");
			}
		}

		return log.toString();
	}

	public void setMonitoredIds(List<String> monitoredIds) {
		this.monitoredIds = monitoredIds;
	}

	public void monitor(String monitoredId) {
		this.monitoredIds.add(monitoredId);
	}

	public DataLayerLogger setPrintAllQueriesLongerThanMS(int printAllQueriesLongerThanMS) {
		this.printAllQueriesLongerThanMS = printAllQueriesLongerThanMS;
		return this;
	}

	public void logAllTransactions() {
		this.logAllTransactions = true;
	}

	public void setSlowQueryDuration(int slowQueryDuration) {
		this.slowQueryDuration = slowQueryDuration;
	}

	public void setVerySlowQueryDuration(int verySlowQueryDuration) {
		this.verySlowQueryDuration = verySlowQueryDuration;
	}

	public boolean isQueryLoggingEnabled() {
		return queryLoggingEnabled;
	}

	public DataLayerLogger setQueryLoggingEnabled(boolean queryLoggingEnabled) {
		this.queryLoggingEnabled = queryLoggingEnabled;
		return this;
	}

	public boolean isLogFL() {
		return logFL;
	}

	public DataLayerLogger setLogFL(boolean logFL) {
		this.logFL = logFL;
		return this;
	}
}
