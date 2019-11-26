
package com.constellio.data.dao.services;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.constellio.data.utils.LoggerUtils.toParamsString;
import static java.util.Arrays.asList;

public class DataLayerSqlLogger {


		private static final Logger LOGGER = LoggerFactory.getLogger(DataLayerLogger.class);

		private List<String> monitoredIds = new ArrayList<>();

		private boolean logFL = true;

		private int printAllQueriesLongerThanMS = 10000;

		private int slowQueryDuration = 20000;

		private int verySlowQueryDuration = 30000;

		public boolean logAllTransactions = false;

		private boolean queryLoggingEnabled = true;

		private boolean queryDebuggingMode;

		private boolean backgroundThreadQueryDebuggingMode = false;

		private String queryDebuggingPrefix;

		public String getQueryDebuggingPrefix() {
			return queryDebuggingPrefix;
		}

		public DataLayerSqlLogger setQueryDebuggingPrefix(String queryDebuggingPrefix) {
			this.queryDebuggingPrefix = queryDebuggingPrefix;
			return this;
		}

		public void logQueryResponse(String queryName, SolrParams params, QueryResponse response) {

			if (queryDebuggingMode
				&& (backgroundThreadQueryDebuggingMode || queryName == null || !queryName.toLowerCase().contains("background"))
				&& (queryName == null || !queryName.contains("*SDK* Validate cache"))) {

				StringBuilder queryReport = new StringBuilder();

				if (queryDebuggingPrefix != null) {
					queryReport.append(queryDebuggingPrefix);
				}

				queryReport.append("\n");
				if (queryName != null) {
					queryReport.append("Name : " + queryName + "\n");
				}
				queryReport.append("Qtime : " + response.getQTime() + "\n");
				queryReport.append("Numfound : " + response.getResults().getNumFound() + "\n");
				queryReport.append("Returned documents : " + response.getResults().size());

				queryReport.append("\n----- ----- ----- QUERY ----- ----- -----");
				queryReport.append("\n");
				String paramString = toParamsString(params, true);
				queryReport.append(paramString);
				queryReport.append("\n");
				queryReport.append("\n----- ----- ----- STACK ----- ----- -----");
				StringWriter sw = new StringWriter();
				new Throwable("").printStackTrace(new PrintWriter(sw));
				queryReport.append("\n");
				queryReport.append(cleanStackTrace(sw.toString()));
				queryReport.append("\n");
				LOGGER.info(queryReport.toString());
			} else if (queryLoggingEnabled) {
				String prefix = null;
				if (response.getQTime() >= verySlowQueryDuration) {
					prefix = "VERY SLOW QUERY : ";

				} else if (response.getQTime() >= slowQueryDuration) {
					prefix = "SLOW QUERY : ";

				} else if (response.getQTime() >= printAllQueriesLongerThanMS) {
					prefix = "QUERY : ";
				}

				if (prefix != null) {
					String paramString = toParamsString(params, false, "qt", "shards.qt", logFL ? "" : "fl");
					if (!paramString.contains("markedForReindexing_s")) {
						long numFound = response.getResults() == null ? 0 : response.getResults().getNumFound();
						long size = response.getResults() == null ? 0 : response.getResults().size();
						LOGGER.info(prefix + "qtime=" + response.getQTime() + ", numfound=" + numFound + ", documents=" + size
									+ "\n" + paramString + "\n");
					}
				}
			}
		}

		private String cleanStackTrace(String string) {
			//			int secondLine = string.indexOf("\n");
			//			int thirdLine = string.indexOf("\n", secondLine + 1);
			//			int fourthLine = string.indexOf("\n", thirdLine + 1);
			//			return string.substring(fourthLine + 1);
			List<String> lines = new ArrayList<>(Arrays.asList(string.split("\n")));

			//Remove the first three lines
			lines.remove(0);
			lines.remove(0);
			lines.remove(0);
			lines.remove(0);

			while (!lines.get(lines.size() - 1).startsWith("\tat com.constellio")) {
				lines.remove(lines.size() - 1);
			}

			return StringUtils.join(lines, "\n");
		}

		public void logTransaction(TransactionDTO transaction, TransactionResponseDTO response) {
			StringBuilder logBuilder = new StringBuilder();

			boolean printStack = false;

			for (RecordDTO recordDTO : transaction.getNewRecords()) {
				if (logAllTransactions) {
					logBuilder.append("\n\t" + toString(recordDTO, response.getNewDocumentVersion(recordDTO.getId())));
				}

				if (monitoredIds.contains(recordDTO.getId())) {
					printStack = true;
					logBuilder.append("\n\t" + toString(recordDTO, response.getNewDocumentVersion(recordDTO.getId())));
				}
			}

			for (RecordDeltaDTO recordDeltaDTO : transaction.getModifiedRecords()) {
				if (logAllTransactions) {
					logBuilder.append("\n\t" + toString(recordDeltaDTO, response.getNewDocumentVersion(recordDeltaDTO.getId())));
				}
			}

			for (RecordDeltaDTO recordDeltaDTO : transaction.getModifiedRecords()) {
				if (monitoredIds.contains(recordDeltaDTO.getId())) {
					printStack = true;
					logBuilder.append("\n\t" + toString(recordDeltaDTO, response.getNewDocumentVersion(recordDeltaDTO.getId())));
				}
			}

			String log = logBuilder.toString();
			if (printStack) {
				log += "\n" + ExceptionUtils.getStackTrace(new RuntimeException());
			}


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

		private String toString(RecordDeltaDTO recordDeltaDTO, long newVersion) {
			StringBuilder log = new StringBuilder();

			log.append(recordDeltaDTO.getId() + "@" + recordDeltaDTO.getFromVersion() + " >> " + newVersion + "\t");
			for (String modifiedField : recordDeltaDTO.getModifiedFields().keySet()) {
				if (!hiddenFieldsInLogs.contains(modifiedField)) {
					log.append(modifiedField);
					log.append(":");
					if (recordDeltaDTO.getInitialFields() != null) {
						log.append(recordDeltaDTO.getInitialFields().get(modifiedField));
					} else {
						log.append("?");
					}
					log.append("=>");
					log.append(recordDeltaDTO.getModifiedFields().get(modifiedField));
					log.append("; ");
				}
			}

			return log.toString();
		}

		private String toString(RecordDTO recordDTO, long newVersion) {
			StringBuilder log = new StringBuilder();

			log.append(recordDTO.getId() + "@" + recordDTO.getVersion() + " >> " + newVersion + "\t");
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

			// TODO incrementedFields

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

		public DataLayerSqlLogger setPrintAllQueriesLongerThanMS(int printAllQueriesLongerThanMS) {
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

		public DataLayerSqlLogger setQueryLoggingEnabled(boolean queryLoggingEnabled) {
			this.queryLoggingEnabled = queryLoggingEnabled;
			return this;
		}

		public boolean isLogFL() {
			return logFL;
		}

		public DataLayerSqlLogger setLogFL(boolean logFL) {
			this.logFL = logFL;
			return this;
		}

		public void setQueryDebuggingMode(boolean queryDebuggingMode) {
			this.queryDebuggingMode = queryDebuggingMode;

		}


}
