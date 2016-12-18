package com.constellio.data.dao.services.transactionLog.writer1;

import static com.constellio.data.dao.services.solr.DateUtils.correctDate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.common.SolrDocumentBase;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.extensions.DataLayerSystemExtensions;

public class TransactionWriterV1 {

	boolean writeZZRecords;

	DataLayerSystemExtensions extensions;

	public TransactionWriterV1(boolean writeZZRecords, DataLayerSystemExtensions extensions) {
		this.extensions = extensions;
		this.writeZZRecords = writeZZRecords;
	}

	public String toSetSequenceLogEntry(String sequenceId, long value) {
		StringBuilder stringBuilder = new StringBuilder("--transaction--\n");
		stringBuilder.append("sequence set " + sequenceId + "=" + value + "\n");
		return stringBuilder.toString();
	}

	public String toNextSequenceLogEntry(String sequenceId) {
		StringBuilder stringBuilder = new StringBuilder("--transaction--\n");
		stringBuilder.append("sequence next " + sequenceId + "\n");
		return stringBuilder.toString();
	}

	public String toLogEntry(BigVaultServerTransaction transaction) {

		StringBuilder stringBuilder = new StringBuilder("--transaction--\n");

		for (SolrInputDocument solrDocument : transaction.getNewDocuments()) {
			String id = (String) solrDocument.getFieldValue("id");
			if (isLogged(id)) {
				appendAddUpdateSolrDocument(stringBuilder, solrDocument);
			}
		}
		for (SolrInputDocument solrDocument : transaction.getUpdatedDocuments()) {
			String id = (String) solrDocument.getFieldValue("id");
			if (isLogged(id)) {
				appendAddUpdateSolrDocument(stringBuilder, solrDocument);
			}
		}

		List<String> deletedIds = new ArrayList<>();
		for (String id : transaction.getDeletedRecords()) {
			if (isLogged(id)) {
				deletedIds.add(id);
			}
		}

		appendDeletedRecords(stringBuilder, deletedIds);
		for (String deletedByQuery : transaction.getDeletedQueries()) {
			appendDeletedByQuery(stringBuilder, deletedByQuery);
		}

		return stringBuilder.toString();
	}

	private void appendDeletedRecords(StringBuilder stringBuilder, List<String> deletedDocumentIds) {
		if (!deletedDocumentIds.isEmpty()) {
			stringBuilder.append("delete");
			for (String deletedDocumentId : deletedDocumentIds) {
				stringBuilder.append(" ");
				stringBuilder.append(deletedDocumentId);
			}
			stringBuilder.append("\n");
		}
	}

	protected void appendAddUpdateSolrDocument(StringBuilder stringBuilder, SolrDocumentBase document) {
		Object id = null;
		try {
			id = document.getFieldValue("id");
		} catch (ClassCastException e) {
			id = document.getFieldValue("id");
			e.printStackTrace();
		}
		Object version = document.getFieldValue("_version_");

		String schema_s;
		Object schema = document.getFieldValue("schema_s");

		if (schema instanceof String) {
			schema_s = (String) schema;
		} else if (schema instanceof Map) {
			schema_s = (String) ((Map) schema).values().iterator().next();
		} else {
			schema_s = null;
			//throw new ImpossibleRuntimeException("Invalid schema of type");
		}
		String collection_s = (String) document.getFieldValue("collection_s");
		stringBuilder.append("addUpdate ");
		stringBuilder.append(id);
		stringBuilder.append(" ");
		stringBuilder.append(version == null ? "-1" : version);
		stringBuilder.append("\n");

		Collection<String> fieldNames = document.getFieldNames();
		for (String name : fieldNames) {
			if (!name.equals("id") && !name.equals("_version_") && isLogged(name, schema_s, collection_s)) {
				Collection<Object> value = removeEmptyStrings(document.getFieldValues(name));

				if (value.isEmpty()) {
					appendValue(stringBuilder, name, "");
				} else {
					for (Object item : value) {

						String fieldLogName = name;
						if (item instanceof Map) {
							Map<String, Object> mapItemValue = ((Map<String, Object>) item);
							String firstKey = mapItemValue.keySet().iterator().next();
							Object mapValue = mapItemValue.get(firstKey);
							fieldLogName = firstKey + " " + name;

							if (mapValue instanceof Collection) {
								Collection<Object> mapValueList = removeEmptyStrings((Collection) mapValue);
								if (mapValueList.isEmpty()) {
									appendValue(stringBuilder, fieldLogName, "");
								} else {
									for (Object mapValueListItem : mapValueList) {
										appendValue(stringBuilder, fieldLogName, mapValueListItem);
									}
								}
							} else {
								appendValue(stringBuilder, fieldLogName, mapValue);
							}

						} else {
							appendValue(stringBuilder, fieldLogName, item);
						}

					}
				}
			}
		}
	}

	private boolean isLogged(String name, String schema, String collection) {
		boolean defaultValue =
				!name.endsWith("content_txt_fr") && !name.endsWith("content_txt_en") && !name.endsWith("contents_txt_fr") && !name
						.endsWith("contents_txt_en");
		return extensions.isDocumentFieldLoggedInTransactionLog(name, schema, collection, defaultValue);
	}

	private Collection<Object> removeEmptyStrings(Collection collection) {
		if (collection == null) {
			collection = new ArrayList();
		}
		if (collection.contains("")) {
			List<Object> values = new ArrayList<>();

			for (Object item : collection) {
				if (!"".equals(item)) {
					values.add(item);
				}
			}

			return values;
		} else {
			return collection;
		}
	}

	private void appendValue(StringBuilder stringBuilder, String fieldLogName, Object item) {
		//if (!"".equals(item)) {
		String correctedValue = correct(item);
		if (correctedValue != null) {
			stringBuilder.append(fieldLogName);
			stringBuilder.append("=");
			stringBuilder.append(correctedValue);
			stringBuilder.append("\n");
		}
		//}
	}

	private boolean isLogged(String id) {
		return writeZZRecords || !id.endsWith("ZZ");
	}

	protected void appendDeletedByQuery(StringBuilder stringBuilder, String deletedByQuery) {
		stringBuilder.append("deletequery " + deletedByQuery + "\n");
	}

	private Pattern lineFeedPattern = Pattern.compile("\n", Pattern.LITERAL);
	private Pattern zPattern = Pattern.compile("Z", Pattern.LITERAL);

	private String correct(Object value) {
		if (value == null || "null".equals(value)) {
			return "";

		} else if (value instanceof Date) {
			LocalDateTime dateTime = new LocalDateTime(value);
			return zPattern.matcher(correctDate(dateTime).toString()).replaceAll(Matcher.quoteReplacement(""));

		} else if (value instanceof LocalDateTime || value instanceof LocalDate) {
			return zPattern.matcher(value.toString()).replaceAll(Matcher.quoteReplacement(""));

		} else {
			return lineFeedPattern.matcher(value.toString()).replaceAll(Matcher.quoteReplacement("__LINEBREAK__"));
		}
	}

}
