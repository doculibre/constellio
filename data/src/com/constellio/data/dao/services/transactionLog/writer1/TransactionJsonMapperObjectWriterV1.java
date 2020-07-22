package com.constellio.data.dao.services.transactionLog.writer1;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.transactionLog.sql.TransactionDocumentLogContent;
import com.constellio.data.dao.services.transactionLog.sql.TransactionLogContent;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.solr.common.SolrDocumentBase;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.constellio.data.dao.services.solr.DateUtils.correctDate;

public class TransactionJsonMapperObjectWriterV1 {

	boolean writeZZRecords;

	DataLayerSystemExtensions extensions;

	public TransactionJsonMapperObjectWriterV1(boolean writeZZRecords, DataLayerSystemExtensions extensions) {
		this.extensions = extensions;
		this.writeZZRecords = writeZZRecords;
	}

	public String toJsonString(TransactionLogContent content) throws JsonProcessingException {

		if (content == null) {
			return "{}";
		}
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

		return ow.writeValueAsString(content);
	}

	public String toJsonString(BigVaultServerTransaction content) throws IOException {

		return toJsonString(toJsonContentEntry(content));
	}

	public String toLogEntry(BigVaultServerTransaction transaction) {

		try {
			return toJsonString(transaction);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public TransactionLogContent toJsonContentEntry(BigVaultServerTransaction transaction) throws IOException {

		TransactionLogContent transactionLogContent = new TransactionLogContent();
		transactionLogContent.setTransactionId(transaction.getTransactionId());

		for (SolrInputDocument solrDocument : transaction.getNewDocuments()) {
			String id = (String) solrDocument.getFieldValue("id");
			if (isLogged(id)) {
				transactionLogContent.addNewDocuments(appendAddUpdateSolrDocument(solrDocument));
			}
		}
		for (SolrInputDocument solrDocument : transaction.getUpdatedDocuments()) {
			String id = (String) solrDocument.getFieldValue("id");
			if (isLogged(id)) {
				transactionLogContent.addUpdatedDocuments(appendAddUpdateSolrDocument(solrDocument));
			}
		}

		for (String id : transaction.getDeletedRecords()) {
			if (isLogged(id)) {
				transactionLogContent.addDeletedRecords(id);
			}
		}

		for (String deletedByQuery : transaction.getDeletedQueries()) {
			transactionLogContent.addDeletedQueries(deletedByQuery);
		}

		return transactionLogContent;
	}

	protected TransactionDocumentLogContent appendAddUpdateSolrDocument(SolrDocumentBase document) throws IOException {

		TransactionDocumentLogContent transactionDocumentLogContent = new TransactionDocumentLogContent();
		String id = (String) document.getFieldValue("id");
		Object version = document.getFieldValue("_version_");
		String version_s;
		if (version instanceof String) {
			version_s = (String) version;
		} else if (version instanceof Long) {
			version_s = "" + (version);
		} else {
			version_s = null;
			//throw new ImpossibleRuntimeException("Invalid schema of type");
		}

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
		transactionDocumentLogContent.setId(id);
		transactionDocumentLogContent.setVersion(version == null ? "-1" : version_s);

		Collection<String> fieldNames = document.getFieldNames();
		for (String name : fieldNames) {
			if (!name.equals("id") && !name.equals("_version_") && isLogged(name, schema_s, collection_s)) {
				Collection<Object> value = removeEmptyStrings(document.getFieldValues(name));

				if (value.isEmpty()) {
					appendValue(transactionDocumentLogContent, name, "");
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
									appendValue(transactionDocumentLogContent, fieldLogName, "");
								} else {
									for (Object mapValueListItem : mapValueList) {
										appendValue(transactionDocumentLogContent, fieldLogName, mapValueListItem);
									}
								}
							} else {
								appendValue(transactionDocumentLogContent, fieldLogName, mapValue);
							}

						} else {
							appendValue(transactionDocumentLogContent, fieldLogName, item);
						}

					}
				}
			}
		}
		return transactionDocumentLogContent;
	}

	private boolean isLogged(String name, String schema, String collection) {
		boolean defaultValue =
				!name.endsWith("content_txt_fr") && !name.endsWith("content_txt_en") && !name.endsWith("contents_txt_fr") && !name
						.endsWith("contents_txt_en");
		return extensions.isDocumentFieldLoggedInTransactionLog(name, schema, collection, defaultValue);
	}

	private Collection<Object> removeEmptyStrings(Collection collection) {
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

	private void appendValue(TransactionDocumentLogContent transactionDocumentLogContent, String fieldLogName,
							 Object item)
			throws IOException {
		//if (!"".equals(item)) {
		String correctedValue = correct(item);
		if (correctedValue != null) {
			transactionDocumentLogContent.addField(fieldLogName, correctedValue);
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
			return zPattern.matcher(correctDate(dateTime).toString()).replaceAll(Matcher.quoteReplacement("")) + "Z";

		} else if (value instanceof LocalDateTime || value instanceof LocalDate) {
			return zPattern.matcher(value.toString()).replaceAll(Matcher.quoteReplacement("")) + "Z";

		} else {
			return lineFeedPattern.matcher(value.toString()).replaceAll(Matcher.quoteReplacement("__LINEBREAK__"));
		}
	}

}