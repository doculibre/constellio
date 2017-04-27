package com.constellio.data.dao.services.bigVault.solr;

import static com.constellio.data.utils.SimpleDateFormatSingleton.getSimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.MultiMapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.servlet.SolrRequestParsers;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

public class SolrUtils {

	public static final LocalDate NULL_ITEM_LOCALDATE = new LocalDate(4242, 4, 2);
	public static final LocalDateTime NULL_ITEM_LOCAL_DATE_TIME = new LocalDateTime(4242, 4, 2, 4, 2, 4, 242);
	public static final Date NULL_DATE_TIME = new LocalDateTime(4242, 6, 6, 6, 42, 42, 666).toDate();
	public static final String NULL_STRING = "__NULL__";
	public static final String SOLR_DATE_FORMATTER_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	private SolrUtils() {
	}

	public static String toString(BigVaultServerTransaction transaction) {

		StringBuilder sb = new StringBuilder();

		if (transaction.getNewDocuments().size() > 0) {
			sb.append("New documents : ");
			for (SolrInputDocument addedDoc : transaction.getNewDocuments()) {
				sb.append("\n" + toString(addedDoc, "\t"));
			}
			sb.append("\n");
		}

		if (transaction.getUpdatedDocuments().size() > 0) {
			sb.append("Updated documents : ");
			for (SolrInputDocument addedDoc : transaction.getUpdatedDocuments()) {
				sb.append("\n" + toString(addedDoc, "\t"));
			}
			sb.append("\n");
		}

		if (transaction.getDeletedRecords().size() > 0) {
			sb.append("Delete ids : " + transaction.getDeletedRecords());
			sb.append("\n");
		}

		if (transaction.getDeletedQueries().size() > 0) {
			sb.append("Delete by queries : " + transaction.getDeletedQueries());
			sb.append("\n");
		}

		return sb.toString();
	}

	private static String toString(SolrInputDocument doc, String indent) {
		StringBuilder sb = new StringBuilder();
		String id = (String) doc.getFieldValue("id");
		sb.append(indent + "Document '" + id + "' {");

		List<String> fieldNames = new ArrayList<>(doc.getFieldNames());
		Collections.sort(fieldNames);
		for (String fieldName : fieldNames) {
			if (!fieldName.equals("id")) {
				sb.append("\n" + indent + "\t" + fieldName + " : " + doc.getFieldValues(fieldName));
			}
		}
		sb.append("}");
		return sb.toString();
	}

	public static String toString(SolrParams params) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");

		if (params != null && params.getParameterNamesIterator() != null) {
			List<String> keyValues = new ArrayList<>();
			Iterator<String> namesIterator = params.getParameterNamesIterator();
			while (namesIterator.hasNext()) {
				String name = namesIterator.next();
				for (String value : params.getParams(name)) {
					keyValues.add(name + "=" + value);
				}
			}
			sb.append(StringUtils.join(keyValues, ", "));
		}

		sb.append("]");
		return sb.toString();
	}

	public static String toIdString(SolrDocumentList list) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		List<String> ids = new ArrayList<>();
		if (list != null && list.iterator() != null) {
			for (SolrDocument solrDocument : list) {
				ids.add((String) solrDocument.getFieldValue("id"));
			}
		}
		sb.append(StringUtils.join(ids, ", "));
		sb.append("]");
		return sb.toString();
	}

	public static Object convertNullToSolrValue(String fieldName) {
		Object convertedFieldValue = null;
		if (isSingleValueStringOrText(fieldName)) {
			convertedFieldValue = null;
		} else if (fieldName.endsWith("_dt") || fieldName.endsWith("_da")) {
			convertedFieldValue = null;
		} else if (fieldName.endsWith("_d")) {
			convertedFieldValue = null;
		} else if (isMultiValueStringOrText(fieldName)) {
			convertedFieldValue = null;
		} else if (fieldName.endsWith("_dts") || fieldName.endsWith("_das")) {
			convertedFieldValue = null;
		} else if (fieldName.endsWith("_ds")) {
			convertedFieldValue = null;
		}
		return convertedFieldValue;
	}

	public static boolean isSingleValueStringOrText(String fieldName) {
		return fieldName.endsWith("_s") || fieldName.endsWith("_t");
	}

	public static boolean isMultiValueStringOrText(String fieldName) {
		return fieldName.endsWith("_ss") || fieldName.endsWith("_txt");
	}

	public static boolean isMultivalue(String fieldName) {
		return isMultiValueStringOrText(fieldName) || fieldName.endsWith("_dts") || fieldName.endsWith("_das")
				|| fieldName.endsWith("_ds") || fieldName.endsWith("_is") || fieldName.endsWith("_fs") || fieldName
				.endsWith("_ls");
	}

	public static String toIdString(SolrInputDocument document) {
		return (String) document.getFieldValue("id");
	}

	public static List<String> toDeleteQueries(List<SolrParams> params) {
		List<String> queries = new ArrayList<>();
		for (SolrParams param : params) {
			queries.add(toDeleteQueries(param));
		}
		return queries;
	}

	public static String toDeleteQueries(SolrParams params) {

		StringBuffer query = new StringBuffer();
		query.append("((");
		query.append(params.get("q"));
		query.append(")");

		if (params.getParams("fq") != null) {
			for (String fq : params.getParams("fq")) {
				query.append(" AND (");
				query.append(fq);
				query.append(")");
			}
		}

		query.append(")");
		return query.toString();
	}

	public static String convertLocalDateTimeToSolrDate(LocalDateTime localDateTime) {

		if (localDateTime == null) {
			localDateTime = null;
		}

		if (localDateTime != null && localDateTime.getYear() < 1900) {
			return localDateTime.withTime(12, 0, 0, 0).toString(SOLR_DATE_FORMATTER_PATTERN);
		} else {
			return getSimpleDateFormat(SOLR_DATE_FORMATTER_PATTERN).format(localDateTime.toDate());
		}
	}

	public static String convertLocalDateToSolrDate(LocalDate localDate) {
		if (localDate == null) {
			return null;
		}
		return convertLocalDateTimeToSolrDate(localDate.toLocalDateTime(LocalTime.MIDNIGHT));
	}

	public static String toSingleQueryString(ModifiableSolrParams params) {
		params = new ModifiableSolrParams(params);
		params.remove("bq");
		return params.toString();
	}
	
	public static ModifiableSolrParams parseQueryString(String queryString) {
		MultiMapSolrParams multiMapSolrParams = SolrRequestParsers.parseQueryString(queryString);
		return new ModifiableSolrParams(multiMapSolrParams);
	}
	
}
