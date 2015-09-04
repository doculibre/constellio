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
package com.constellio.model.services.search.query.logical;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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

		filterQueries.add(query.getFreeTextQuery());
		filterQueries.add(query.getCondition().getSolrQuery());
		String conditionSignature = StringUtils.join(filterQueries, ",");

		return new LogicalSearchQuerySignature(conditionSignature, query.getSort());
	}

	public boolean isSameCondition(LogicalSearchQuerySignature signature) {
		return conditionSignature.equals(signature.conditionSignature);
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
