package com.constellio.data.dao.dto.records;

import java.util.List;
import java.util.Map;

public class QueryResponseDTO {

	private List<RecordDTO> results;

	private List<MoreLikeThisDTO> moreLikeThisResults;

	private int qtime;

	private long numFound;

	private Map<String, List<FacetValue>> fieldFacetValues;
	private Map<String, List<FacetPivotValue>> fieldFacetPivotValues;
	private Map<String, Map<String, Object>> fieldsStatistics;
	private Map<String, Map<String, List<String>>> highlights;
	private Map<String, Integer> queryFacetValues;

	private boolean correctlySpelt;
	private List<String> spellCheckerSuggestions;

	public QueryResponseDTO(List<RecordDTO> results, int qtime, long numFound,
							Map<String, List<FacetValue>> fieldFacetValues,
							Map<String, List<FacetPivotValue>> fieldFacetPivotValues,
							Map<String, Map<String, Object>> fieldsStatistics,
							Map<String, Integer> queryFacetValues, Map<String, Map<String, List<String>>> highlights,
							boolean correctlySpelt,
							List<String> spellCheckerSuggestions, List<MoreLikeThisDTO> moreLikeThisResults) {
		this.results = results;
		this.qtime = qtime;
		this.numFound = numFound;
		this.fieldFacetValues = fieldFacetValues;
		this.fieldFacetPivotValues = fieldFacetPivotValues;
		this.fieldsStatistics = fieldsStatistics;
		this.queryFacetValues = queryFacetValues;
		this.highlights = highlights;
		this.correctlySpelt = correctlySpelt;
		this.spellCheckerSuggestions = spellCheckerSuggestions;
		this.moreLikeThisResults = moreLikeThisResults;
	}

	public List<RecordDTO> getResults() {
		return results;
	}

	public List<MoreLikeThisDTO> getMoreLikeThisResults() {
		return moreLikeThisResults;
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

	public Map<String, List<FacetPivotValue>> getFieldFacetPivotValues() {
		return fieldFacetPivotValues;
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
