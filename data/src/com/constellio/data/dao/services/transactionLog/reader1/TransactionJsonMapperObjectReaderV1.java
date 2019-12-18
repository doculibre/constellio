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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.HashMap;
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
			TransactionDocumentLogContent transactionLogs1Content = objectMapper.readValue(json, TransactionDocumentLogContent.class);
			return transactionLogs1Content;
		} catch (IOException e) {
			throw new SecondTransactionLogRuntimeException_CannotParseJsonLogCommand(json, e);
		}
	}

	public BigVaultServerTransaction reBuildBigVaultServerTransaction(String transactionDocumentLogContent) {

		return reBuildBigVaultServerTransaction(transactionDocumentLogSqlContentDeserialize(transactionDocumentLogContent));
	}

	public BigVaultServerTransaction reBuildBigVaultServerTransaction(
			TransactionDocumentLogContent transactionDocumentLogContent) {

		if (transactionDocumentLogContent != null) {
			return null;
		}
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW());


		addOperationToTransaction(transaction, transactionDocumentLogContent);


		return transaction;
	}

	private void addOperationToTransaction(BigVaultServerTransaction transaction,
										   TransactionDocumentLogContent transactionDocumentLogContent) {


		String id = transactionDocumentLogContent.getId();
		String version = transactionDocumentLogContent.getVersion();
		SolrInputDocument document = buildAddUpdateDocument(transactionDocumentLogContent.getFields(), id);
		addUpdate(transaction, document, version);

	}

	private SolrInputDocument buildAddUpdateDocument(Map<String, String> currentAddUpdateLines, String id) {
		KeyListMap<String, Object> fieldValues = new KeyListMap<>();


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

	private SolrInputDocument buildAddUpdateDocument(String id, KeyListMap<String, Object> fieldValues) {
		SolrInputDocument inputDocument = new ConstellioSolrInputDocument();
		inputDocument.setField("id", id);
		for (Map.Entry<String, List<Object>> entry : fieldValues.getMapEntries()) {
			String fieldName = entry.getKey();
			String atomicOperation = null;
			int indexOfSpace = fieldName.indexOf(" ");
			if (indexOfSpace != -1) {
				atomicOperation = fieldName.substring(0, indexOfSpace);
				fieldName = fieldName.substring(indexOfSpace + 1);
			}

			List<Object> values = entry.getValue();

			Object value = entry.getValue();
			if (!SolrUtils.isMultivalue(fieldName)) {
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
