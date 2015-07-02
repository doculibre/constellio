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
package com.constellio.data.dao.dto.records;

import java.util.List;
import java.util.Map;

public class QueryResponseDTO {

	private List<RecordDTO> results;

	private int qtime;

	private long numFound;

	private Map<String, List<FacetValue>> fieldFacetValues;
	private Map<String, Map<String, Object>> fieldsStatistics;
	private Map<String, Map<String, List<String>>> highlights;
	private Map<String, Integer> queryFacetValues;

	private boolean correctlySpelt;
	private List<String> spellCheckerSuggestions;

	public QueryResponseDTO(List<RecordDTO> results, int qtime, long numFound, Map<String, List<FacetValue>> fieldFacetValues,
							Map<String, Map<String, Object>> fieldsStatistics,
			Map<String, Integer> queryFacetValues, Map<String, Map<String, List<String>>> highlights, boolean correctlySpelt,
			List<String> spellCheckerSuggestions) {
		this.results = results;
		this.qtime = qtime;
		this.numFound = numFound;
		this.fieldFacetValues = fieldFacetValues;
		this.fieldsStatistics = fieldsStatistics;
		this.queryFacetValues = queryFacetValues;
		this.highlights = highlights;
		this.correctlySpelt = correctlySpelt;
		this.spellCheckerSuggestions = spellCheckerSuggestions;
	}

	public List<RecordDTO> getResults() {
		return results;
	}

	public long getQtime() {
		return qtime;
	}

	public long getNumFound() {
		return numFound;
	}

	public Map<String, List<FacetValue>> getFieldFacetValues() {
		return fieldFacetValues;
	}

	public Map<String, Integer> getQueryFacetValues() {
		return queryFacetValues;
	}

	public Map<String, Map<String, List<String>>> getHighlights() {
		return highlights;
	}

	public boolean isCorrectlySpelt() {
		return correctlySpelt;
	}

	public List<String> getSpellCheckerSuggestions() {
		return spellCheckerSuggestions;
	}

	public Map<String, Map<String, Object>> getFieldsStatistics() {
		return fieldsStatistics;
	}
}
