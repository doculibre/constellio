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
package com.constellio.data.dao.services;

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
import com.constellio.data.utils.LoggerUtils;

public class DataLayerLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataLayerLogger.class);

	private List<String> monitoredIds = new ArrayList<>();

	private int printAllQueriesLongerThanMS = 100;

	private int slowQueryDuration = 1000;

	private int verySlowQueryDuration = 2000;

	private boolean logAllTransactions = false;

	public void logQueryResponse(SolrParams params, QueryResponse response) {

		String prefix = null;
		if (response.getQTime() >= verySlowQueryDuration) {
			prefix = "VERY SLOW QUERY : ";

		} else if (response.getQTime() >= slowQueryDuration) {
			prefix = "SLOW QUERY : ";

		} else if (response.getQTime() >= printAllQueriesLongerThanMS) {
			prefix = "QUERY : ";
		}

		if (prefix != null) {
			LOGGER.info(prefix + "qtime=" + response.getQTime() + ", numfound=" + response.getResults().getNumFound()
					+ ", query=' : '" + LoggerUtils.toParamsString(params) + "'");
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

	public void setPrintAllQueriesLongerThanMS(int printAllQueriesLongerThanMS) {
		this.printAllQueriesLongerThanMS = printAllQueriesLongerThanMS;
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
}
