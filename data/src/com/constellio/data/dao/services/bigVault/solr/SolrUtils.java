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
package com.constellio.data.dao.services.bigVault.solr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
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
			convertedFieldValue = NULL_STRING;
		} else if (fieldName.endsWith("_dt") || fieldName.endsWith("_da")) {
			convertedFieldValue = new SimpleDateFormat(SOLR_DATE_FORMATTER_PATTERN).format(NULL_DATE_TIME);
		} else if (fieldName.endsWith("_d")) {
			convertedFieldValue = Integer.MIN_VALUE;
		} else if (isMultiValueStringOrText(fieldName)) {
			convertedFieldValue = new ArrayList<>(Arrays.asList(NULL_STRING));
		} else if (fieldName.endsWith("_dts") || fieldName.endsWith("_das")) {
			convertedFieldValue = new ArrayList<>(Arrays.asList(new SimpleDateFormat(SOLR_DATE_FORMATTER_PATTERN).format(
					NULL_DATE_TIME)));
		} else if (fieldName.endsWith("_ds")) {
			convertedFieldValue = new ArrayList<>(Arrays.asList(Integer.MIN_VALUE));
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
			localDateTime = NULL_ITEM_LOCAL_DATE_TIME;
		}

		if (localDateTime != NULL_ITEM_LOCAL_DATE_TIME && localDateTime.getYear() < 1900) {
			return localDateTime.withTime(12, 0, 0, 0).toString(SOLR_DATE_FORMATTER_PATTERN);
		} else {
			return new SimpleDateFormat(SOLR_DATE_FORMATTER_PATTERN).format(localDateTime.toDate());
		}
	}

	public static String convertLocalDateToSolrDate(LocalDate localDate) {
		if (localDate == null) {
			localDate = NULL_ITEM_LOCALDATE;
		}
		return convertLocalDateTimeToSolrDate(localDate.toLocalDateTime(LocalTime.MIDNIGHT));
	}
}
