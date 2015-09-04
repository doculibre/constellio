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
package com.constellio.model.services.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.model.entities.records.Record;

public class SPEQueryResponse {

	private final Map<String, List<FacetValue>> fieldFacetValues;
	private final Map<String, Map<String, Object>> statisticsValues;

	private final Map<String, Integer> queryFacetsValues;

	private final Map<String, Map<String, List<String>>> highlights;

	private final long qtime;

	private final long numFound;

	private final List<Record> records;

	private final boolean correctlySpelt;
	private final List<String> spellcheckerSuggestions;

	public SPEQueryResponse(List<Record> records) {
		this.fieldFacetValues = new HashMap<>();
		this.statisticsValues = new HashMap<>();
		this.queryFacetsValues = new HashMap<>();
		this.qtime = -1;
		this.numFound = records.size();
		this.records = records;
		this.highlights = new HashMap<>();
		this.correctlySpelt = true;
		this.spellcheckerSuggestions = new ArrayList<>();
	}

	public SPEQueryResponse(
			Map<String, List<FacetValue>> fieldFacetValues, Map<String, Map<String, Object>> statisticsValues,
			Map<String, Integer> queryFacetsValues, long qtime,
			long numFound, List<Record> records, Map<String, Map<String, List<String>>> highlights, boolean correctlySpelt,
			List<String> spellcheckerSuggestions) {
		this.fieldFacetValues = fieldFacetValues;
		this.statisticsValues = statisticsValues;
		this.queryFacetsValues = queryFacetsValues;
		this.qtime = qtime;
		this.numFound = numFound;
		this.records = records;
		this.highlights = highlights;
		this.correctlySpelt = correctlySpelt;
		this.spellcheckerSuggestions = spellcheckerSuggestions;
	}

	public List<FacetValue> getFieldFacetValues(String metadata) {
		if (fieldFacetValues.containsKey(metadata)) {
			return fieldFacetValues.get(metadata);
		} else {
			return Collections.emptyList();
		}
	}

	public Map<String, Object> getStatValues(String metadata) {
		return this.statisticsValues.get(metadata);
	}

	public Integer getQueryFacetCount(String query) {
		return queryFacetsValues.get(query);
	}

	public List<String> getFieldFacetValuesWithResults(String field) {
		List<String> values = new ArrayList<>();

		for (FacetValue facetValue : getFieldFacetValues(field)) {
			if (facetValue.getQuantity() > 0) {
				values.add(facetValue.getValue());
			}
		}

		return values;
	}

	public List<Record> getRecords() {
		return records;
	}

	public long getQtime() {
		return qtime;
	}

	public long getNumFound() {
		return numFound;
	}

	public FacetValue getQueryFacetValue(String value) {
		int count = !queryFacetsValues.containsKey(value) ? 0 : queryFacetsValues.get(value);
		return new FacetValue(value, count);
	}

	public FacetValue getFieldFacetValue(String datastoreCode, String value) {
		if (fieldFacetValues.containsKey(datastoreCode)) {
			for (FacetValue facetValue : getFieldFacetValues(datastoreCode)) {
				if (facetValue.getValue().equals(value)) {
					return facetValue;
				}
			}
		}
		return null;
	}

	public Map<String, List<FacetValue>> getFieldFacetValues() {
		return fieldFacetValues;
	}

	public Map<String, Integer> getQueryFacetsValues() {
		return queryFacetsValues;
	}

	public SPEQueryResponse withModifiedRecordList(List<Record> records) {
		return new SPEQueryResponse(fieldFacetValues, statisticsValues, queryFacetsValues, qtime, numFound, records, null,
				correctlySpelt,
				spellcheckerSuggestions);
	}

	public SPEQueryResponse withNumFound(int numFound) {
		return new SPEQueryResponse(fieldFacetValues, statisticsValues, queryFacetsValues, qtime, numFound, records, null,
				correctlySpelt,
				spellcheckerSuggestions);
	}

	public Map<String, Map<String, List<String>>> getHighlights() {
		return highlights;
	}

	public boolean isCorrectlySpelt() {
		return correctlySpelt;
	}

	public List<String> getSpellCheckerSuggestions() {
		return spellcheckerSuggestions;
	}

	public Map<String, List<String>> getHighlighting(String recordId) {
		if (highlights == null) {
			return Collections.emptyMap();
		} else {
			return highlights.get(recordId);
		}
	}
}
