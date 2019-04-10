package com.constellio.model.services.search.query.logical;

import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderContext;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LogicalSearchQuerySignature implements Serializable {

	String conditionSignature;

	String sortSignature;

	String language;

	private LogicalSearchQuerySignature(String conditionSignature, String sortSignature, String language) {
		this.conditionSignature = conditionSignature;
		this.sortSignature = sortSignature;
		this.language = language;
	}

	public static LogicalSearchQuerySignature signature(LogicalSearchQuery query) {
		List<String> filterQueries = new ArrayList<>();
		filterQueries.addAll(query.getFilterQueries());
		Collections.sort(filterQueries);

		SolrQueryBuilderContext params = new SolrQueryBuilderContext(query.isPreferAnalyzedFields(), new ArrayList<>(), "?", null, null, null);
		filterQueries.add(query.getFreeTextQuery());
		filterQueries.add(query.getCondition().getSolrQuery(params));
		String conditionSignature = StringUtils.join(filterQueries, ",");

		String sortSignature = toSortSignature(query.getSortFields(), query.getLanguage());

		return new LogicalSearchQuerySignature(conditionSignature, sortSignature, query.getLanguage());
	}

	private static String toSortSignature(List<LogicalSearchQuerySort> sortFields, String language) {
		StringBuilder signatureBuilder = new StringBuilder();

		for (LogicalSearchQuerySort sortField : sortFields) {
			if (sortField instanceof FieldLogicalSearchQuerySort) {
				signatureBuilder.append(((FieldLogicalSearchQuerySort) sortField).getField().getDataStoreCode());

			} else if (sortField instanceof FunctionLogicalSearchQuerySort) {
				signatureBuilder.append(((FunctionLogicalSearchQuerySort) sortField).getFunction());

			} else if (sortField instanceof ScoreLogicalSearchQuerySort) {
				signatureBuilder.append(((ScoreLogicalSearchQuerySort) sortField).getField());
			} else {
				throw new IllegalArgumentException("Unsupported sort field of type " + sortField.getClass().getSimpleName());

			}
			signatureBuilder.append(sortField.isAscending() ? "1" : "0");
		}
		if (language != null) {
			signatureBuilder.append(language);
		}

		return signatureBuilder.toString();
	}

	public boolean isSameCondition(LogicalSearchQuerySignature signature) {
		return conditionSignature.equals(signature.conditionSignature);
	}

	public String toStringSignature() {
		return conditionSignature + ":" + sortSignature;
	}

	public boolean isSameSort(LogicalSearchQuerySignature signature) {
		return sortSignature.equals(signature.sortSignature);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof LogicalSearchQuerySignature)) {
			return false;
		}
		LogicalSearchQuerySignature that = (LogicalSearchQuerySignature) o;
		return Objects.equals(conditionSignature, that.conditionSignature) &&
			   Objects.equals(sortSignature, that.sortSignature) &&
			   Objects.equals(language, that.language);
	}

	@Override
	public int hashCode() {

		return Objects.hash(conditionSignature, sortSignature, language);
	}
}
