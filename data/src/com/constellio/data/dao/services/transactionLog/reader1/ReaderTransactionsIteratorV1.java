package com.constellio.data.dao.services.transactionLog.reader1;

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
import com.constellio.data.utils.LangUtils.StringReplacer;
import com.constellio.data.utils.LazyIterator;
import com.constellio.data.utils.dev.Toggle;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.constellio.data.utils.LangUtils.isEmptyList;
import static com.constellio.data.utils.LangUtils.replacingLiteral;

public class ReaderTransactionsIteratorV1 extends LazyIterator<BigVaultServerTransaction> {

	protected String fileName;
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
				addOperationToTransaction(transaction, currentAddUpdateLines);
				currentAddUpdateLines.clear();
			}
			currentAddUpdateLines.add(line);
		}

		if (!currentAddUpdateLines.isEmpty()) {
			addOperationToTransaction(transaction, currentAddUpdateLines);
		}

		if (transaction.isEmpty()) {
			return getNextOrNull();
		} else {
			return transaction;
		}


	}

	private void addOperationToTransaction(BigVaultServerTransaction transaction, List<String> currentAddUpdateLines) {
		String firstLine = currentAddUpdateLines.get(0);
		if (firstLine.startsWith("addUpdate ")) {
			handleAddUpdateLine(transaction, currentAddUpdateLines, firstLine);

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

	protected void handleSequenceSetLine(BigVaultServerTransaction transaction, String firstLine) {
		String infos = firstLine.substring("sequence set ".length());
		String sequenceId = StringUtils.substringBeforeLast(infos, "=");
		long value = Long.valueOf(StringUtils.substringAfterLast(infos, "="));
		transaction.getNewDocuments().add(SolrSequencesManager.setSequenceInLogReplay(sequenceId, value));
	}

	protected void handleSequenceNextLine(BigVaultServerTransaction transaction, String firstLine) {
		String sequenceId = firstLine.substring("sequence next ".length());
		transaction.getNewDocuments().add(SolrSequencesManager.incrementSequenceInLogReplay(sequenceId));
	}

	protected void handleDeleteQueryOperation(BigVaultServerTransaction transaction, String firstLine) {
		int index = firstLine.indexOf(" ");
		String query = firstLine.substring(index);
		transaction.getDeletedQueries().add(query);
	}

	protected void handleDeleteOperation(BigVaultServerTransaction transaction, String firstLine) {
		int index = firstLine.indexOf(" ");
		List<String> ids = Arrays.asList(firstLine.substring(index).split(" "));
		transaction.getDeletedRecords().addAll(ids);
	}

	protected void handleAddUpdateLine(BigVaultServerTransaction transaction, List<String> currentAddUpdateLines,
									   String firstLine) {
		String[] firstLineParts = firstLine.split(" ");
		if (firstLineParts.length != 3) {
			throw new ImpossibleRuntimeException("Unsupported first line '" + firstLine + "'");
		}

		String id = firstLineParts[1];
		String version = firstLineParts[2];
		SolrInputDocument document = buildAddUpdateDocument(currentAddUpdateLines, id);

		if (!isSkipped(document)) {

//			if (document.getFieldNames().size() < 10) {
			//				System.out.println("Small transaction : " + document.getFieldNames());
			//				for (String fieldName : document.getFieldNames()) {
			//					System.out.println(" - " + fieldName + "= " + document.getFieldValue(fieldName));
			//				}
			//				System.out.println("");
			//			}

			addUpdate(transaction, document, version);
		}
	}

	private boolean isSkipped(SolrInputDocument document) {
		if (!Toggle.MARKED_RECORDS_IN_SAVESTATES_DISABLED.isEnabled()) {
			return false;
		}
		if (document.size() == 2 && (document.getFieldNames().contains("markedForReindexing_s")
									 || document.getFieldNames().contains("markedForPreviewConversion_s")
									 || document.getFieldNames().contains("markedForParsing_s"))) {
			return true;
		}

		if (document.size() < 10 && document.getFieldNames().contains("markedForPreviewConversion_s")) {
			return hasInterestingValues(document);
		}

		return false;
	}

	private boolean hasInterestingValues(SolrInputDocument document) {
		boolean hasFieldWithStuff = false;
		for (String fieldName : document.getFieldNames()) {
			if (!fieldName.endsWith("txt_fr") && !fieldName.equals("id")
				&& !fieldName.equals("markedForReindexing_s")
				&& !fieldName.equals("markedForPreviewConversion_s")
				&& !fieldName.equals("markedForParsing_s")
				&& !fieldName.equals("migrationDataVersion_d")
				&& !fieldName.equals("estimatedSize_i")) {
				Object value = document.getFieldValue(fieldName);
				if (value != null && !isSetNullMap(value)) {
					hasFieldWithStuff = true;
				}
			}
		}
		return !hasFieldWithStuff;
	}

	private boolean isSetNullMap(Object value) {
		if (value == null) {
			return false;
		}
		if (value instanceof Map) {
			Map map = (Map) value;
			if (map.containsKey("set")) {

				Object settedValue = map.get("set");
				return settedValue == null || isEmptyList(settedValue);

			} else {
				return false;
			}

		} else {
			return false;
		}
	}

	private SolrInputDocument buildAddUpdateDocument(List<String> currentAddUpdateLines, String id) {
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

	private StringReplacer stringReplacer = replacingLiteral("__LINEBREAK__", "\n");

	private Object convertValueForLogReplay(String field, String value) {
		return stringReplacer.replaceOn(value);
	}

	private boolean isFirstLineOfOperation(String line) {
		return line.startsWith("addUpdate ") || line.startsWith("delete ") || line.startsWith("deletequery ");
	}

}
