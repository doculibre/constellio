package com.constellio.model.services.search.query.logical;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderParams;

public class LogicalSearchQuerySignature implements Serializable {

	String conditionSignature;

	String sortSignature;

	private LogicalSearchQuerySignature(String conditionSignature, String sortSignature) {
		this.conditionSignature = conditionSignature;
		this.sortSignature = sortSignature;
	}

	public static LogicalSearchQuerySignature signature(LogicalSearchQuery query) {
		List<String> filterQueries = new ArrayList<>();
		filterQueries.addAll(query.getFilterQueries());
		Collections.sort(filterQueries);

		SolrQueryBuilderParams params = new SolrQueryBuilderParams(query.isPreferAnalyzedFields(), "?");
		filterQueries.add(query.getFreeTextQuery());
		filterQueries.add(query.getCondition().getSolrQuery(params));
		String conditionSignature = StringUtils.join(filterQueries, ",");

		return new LogicalSearchQuerySignature(conditionSignature, query.getSort());
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
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		LogicalSearchQuerySignature that = (LogicalSearchQuerySignature) o;

		if (!conditionSignature.equals(that.conditionSignature)) {
			return false;
		}
		if (!sortSignature.equals(that.sortSignature)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = conditionSignature.hashCode();
		result = 31 * result + sortSignature.hashCode();
		return result;
	}
}
