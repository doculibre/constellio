package com.constellio.data.dao.services.transactionLog.reader1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.dao.services.sequence.SolrSequencesManager;
import com.constellio.data.dao.services.solr.ConstellioSolrInputDocument;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogReplayFilter;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogRuntimeException.SecondTransactionLogRuntimeException_CannotParseLogCommand;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.KeyListMap;
import com.constellio.data.utils.LazyIterator;

public class ReaderTransactionsIteratorV1 extends LazyIterator<BigVaultServerTransaction> {

	String fileName;
	Iterator<List<String>> transactionLinesIterator;
	DataLayerConfiguration configuration;

	public ReaderTransactionsIteratorV1(String fileName, Iterator<List<String>> transactionLinesIterator,
			DataLayerConfiguration configuration) {
		this.fileName = fileName;
		this.transactionLinesIterator = transactionLinesIterator;
		this.configuration = configuration;
	}

	@Override
	protected BigVaultServerTransaction getNextOrNull() {
		if (!transactionLinesIterator.hasNext()) {
			return null;
		}
		BigVaultServerTransaction transaction = new BigVaultServerTransaction(RecordsFlushing.NOW());

		List<String> currentAddUpdateLines = new ArrayList<>();
		for (String line : transactionLinesIterator.next()) {
			if (isFirstLineOfOperation(line) && !currentAddUpdateLines.isEmpty()) {
				addOperationToTransaction(fileName, transaction, currentAddUpdateLines);
				currentAddUpdateLines.clear();
			}
			currentAddUpdateLines.add(line);
		}

		if (!currentAddUpdateLines.isEmpty()) {
			addOperationToTransaction(fileName, transaction, currentAddUpdateLines);
		}

		return transaction;
	}

	private void addOperationToTransaction(String fileName, BigVaultServerTransaction transaction,
			List<String> currentAddUpdateLines) {
		String firstLine = currentAddUpdateLines.get(0);
		if (firstLine.startsWith("addUpdate ")) {
			handleAddUpdateLine(fileName, transaction, currentAddUpdateLines, firstLine);

		} else if (firstLine.startsWith("delete ")) {
			handleDeleteOperation(transaction, firstLine);

		} else if (firstLine.startsWith("deletequery ")) {
			handleDeleteQueryOperation(transaction, firstLine);

		} else if (firstLine.startsWith("sequence next ")) {
			handleSequenceNextLine(transaction, firstLine);

		} else if (firstLine.startsWith("sequence set ")) {
			handleSequenceSetLine(transaction, firstLine);

		}

	}

	private void handleSequenceSetLine(BigVaultServerTransaction transaction, String firstLine) {
		String infos = firstLine.substring("sequence set ".length());
		String sequenceId = StringUtils.substringBeforeLast(infos, "=");
		long value = Long.valueOf(StringUtils.substringAfterLast(infos, "="));
		transaction.getNewDocuments().add(SolrSequencesManager.setSequenceInLogReplay(sequenceId, value));
	}

	private void handleSequenceNextLine(BigVaultServerTransaction transaction, String firstLine) {
		String sequenceId = firstLine.substring("sequence next ".length());
		transaction.getNewDocuments().add(SolrSequencesManager.incrementSequenceInLogReplay(sequenceId));
	}

	private void handleDeleteQueryOperation(BigVaultServerTransaction transaction, String firstLine) {
		int index = firstLine.indexOf(" ");
		String query = firstLine.substring(index);
		transaction.getDeletedQueries().add(query);
	}

	private void handleDeleteOperation(BigVaultServerTransaction transaction, String firstLine) {
		int index = firstLine.indexOf(" ");
		List<String> ids = Arrays.asList(firstLine.substring(index).split(" "));
		transaction.getDeletedRecords().addAll(ids);
	}

	private void handleAddUpdateLine(String fileName, BigVaultServerTransaction transaction, List<String> currentAddUpdateLines,
			String firstLine) {
		String[] firstLineParts = firstLine.split(" ");
		if (firstLineParts.length != 3) {
			throw new ImpossibleRuntimeException("Unsupported first line '" + firstLine + "'");
		}

		String id = firstLineParts[1];
		String version = firstLineParts[2];
		SolrInputDocument document = buildAddUpdateDocument(fileName, currentAddUpdateLines, id);
		addUpdate(transaction, document, version);
	}

	private SolrInputDocument buildAddUpdateDocument(String fileName, List<String> currentAddUpdateLines, String id) {
		KeyListMap<String, Object> fieldValues = new KeyListMap<>();

		try {
			for (int i = 1; i < currentAddUpdateLines.size(); i++) {

				String line = currentAddUpdateLines.get(i);
				int indexOfEqualSign = line.indexOf("=");
				String field = line.substring(0, indexOfEqualSign);
				String value = line.substring(indexOfEqualSign + 1);
				Object convertedValue = convertValueForLogReplay(field, value);

				fieldValues.add(field, convertedValue);

			}
		} catch (RuntimeException e) {
			throw new SecondTransactionLogRuntimeException_CannotParseLogCommand(currentAddUpdateLines, fileName, e);
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

	private Object convertValueForLogReplay(String field, String value) {
		return value.replace("__LINEBREAK__", "\n");
	}

	private boolean isFirstLineOfOperation(String line) {
		return line.startsWith("addUpdate ") || line.startsWith("delete ") || line.startsWith("deletequery ");
	}

}
