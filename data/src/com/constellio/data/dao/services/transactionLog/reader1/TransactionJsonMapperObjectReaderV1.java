package com.constellio.data.dao.services.transactionLog.reader1;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.dao.services.solr.ConstellioSolrInputDocument;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogReplayFilter;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_CannotParseJsonLogCommand;
import com.constellio.data.dao.services.transactionLog.sql.TransactionDocumentLogContent;
import com.constellio.data.dao.services.transactionLog.sql.TransactionLogContent;
import com.constellio.data.utils.KeyListMap;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TransactionJsonMapperObjectReaderV1 {


	DataLayerConfiguration configuration;

	public TransactionJsonMapperObjectReaderV1(
			DataLayerConfiguration configuration) {


		this.configuration = configuration;
	}

	public TransactionLogContent transactionLogSqlContentDeserialize(String json) throws IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		TransactionLogContent transactionLogs1Content = objectMapper.readValue(json, TransactionLogContent.class);
		return transactionLogs1Content;
	}

	public TransactionDocumentLogContent transactionDocumentLogSqlContentDeserialize(String json) {

		ObjectMapper objectMapper = new ObjectMapper();
		try {
			TransactionDocumentLogContent transactionLogsContent = objectMapper.readValue(json, TransactionDocumentLogContent.class);
			return transactionLogsContent;
		} catch (IOException e) {
			throw new SecondTransactionLogRuntimeException_CannotParseJsonLogCommand(json, e);
		}
	}


	public List<TransactionDocumentLogContent> transactionDocumentLogSqlContentArrayDeserialize(String json) {

		ObjectMapper objectMapper = new ObjectMapper();
		try {
			List<TransactionDocumentLogContent> transactionLogsContent = Arrays.asList(objectMapper.readValue(json,
					TransactionDocumentLogContent[].class));
			return transactionLogsContent;
		} catch (IOException e) {
			throw new SecondTransactionLogRuntimeException_CannotParseJsonLogCommand(json, e);
		}
	}

	public BigVaultServerTransaction reBuildBigVaultServerTransaction(String transactionDocumentLogContent)
			throws IOException {

		return reBuildBigVaultServerTransaction(transactionDocumentLogSqlContentDeserialize(transactionDocumentLogContent));
	}

	public BigVaultServerTransaction reBuildBigVaultServerTransaction(
			TransactionDocumentLogContent transactionDocumentLogContent) throws IOException {

		if (transactionDocumentLogContent == null) {
			return null;
		}
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW());


		addOperationToTransaction(transaction, transactionDocumentLogContent);


		return transaction;
	}


	public BigVaultServerTransaction reBuildBigVaultServerTransactionArray(String content) {
		return reBuildBigVaultServerTransactionArray(transactionDocumentLogSqlContentArrayDeserialize(content));
	}


	public BigVaultServerTransaction reBuildBigVaultServerTransactionArray(
			List<TransactionDocumentLogContent> transactionDocumentLogContent) {

		if (transactionDocumentLogContent == null) {
			return null;
		}
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW());

		try {
			addOperationsToTransaction(transaction, transactionDocumentLogContent);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		return transaction;
	}

	private void addOperationToTransaction(BigVaultServerTransaction transaction,
										   TransactionDocumentLogContent transactionDocumentLogContent)
			throws IOException {


		String id = transactionDocumentLogContent.getId();
		String version = transactionDocumentLogContent.getVersion();
		SolrInputDocument document = buildAddUpdateDocument(transactionDocumentLogContent.getFields(), id);
		addUpdate(transaction, document, version);

	}

	private void addOperationsToTransaction(BigVaultServerTransaction transaction,
											List<TransactionDocumentLogContent> transactionDocumentLogContents)
			throws IOException {


		for (TransactionDocumentLogContent transactionDocumentLogContent : transactionDocumentLogContents) {
			String id = transactionDocumentLogContent.getId();
			String version = transactionDocumentLogContent.getVersion();
			SolrInputDocument document = buildAddUpdateDocument(transactionDocumentLogContent.getFields(), id);

			addUpdate(transaction, document, version);
		}

	}

	private SolrInputDocument buildAddUpdateDocument(Map<String, String> currentAddUpdateLines, String id)
			throws IOException {
		KeyListMap<String, String> fieldValues = new KeyListMap<>();


		for (Entry<String, String> documentField : currentAddUpdateLines.entrySet()) {

			String field = documentField.getKey();
			String value = documentField.getValue();

			fieldValues.add(field, value);
		}

		return buildAddUpdateDocument(id, fieldValues);
	}

	public void addUpdate(BigVaultServerTransaction transaction, SolrInputDocument document, String version) {
		SecondTransactionLogReplayFilter filter = configuration.getSecondTransactionLogReplayFilter();
		String id = (String) document.getFieldValue("id");

		if (version.equals("-1")) {
			String schema = (String) document.getFieldValue("schema_s");
			if (filter.isReplayingAdd(id, schema, document)) {
				transaction.getNewDocuments().add(document);
			}
		} else {
			if (filter.isReplayingUpdate(id, document)) {
				transaction.getUpdatedDocuments().add(document);
			}
		}
	}

	private SolrInputDocument buildAddUpdateDocument(String id, KeyListMap<String, String> fieldValues)
			throws IOException {
		SolrInputDocument inputDocument = new ConstellioSolrInputDocument();
		inputDocument.setField("id", id);
		for (Map.Entry<String, List<String>> entry : fieldValues.getMapEntries()) {
			String fieldName = entry.getKey();
			String atomicOperation = null;
			int indexOfSpace = fieldName.indexOf(" ");
			if (indexOfSpace != -1) {
				atomicOperation = fieldName.substring(0, indexOfSpace);
				fieldName = fieldName.substring(indexOfSpace + 1);
			}

			Object value;
			if (entry.getValue().get(0).startsWith("[")) {
				ObjectMapper mapper = new ObjectMapper();
				value = Arrays.asList(mapper.readValue(entry.getValue().get(0), String[].class));
			} else {
				value = entry.getValue().get(0);
			}

			if (atomicOperation != null) {
				Map<String, Object> setValue = new HashMap<>();
				setValue.put(atomicOperation, value);
				inputDocument.setField(fieldName, setValue);
			} else {
				inputDocument.setField(fieldName, value);
			}
		}
		return inputDocument;
	}

}
