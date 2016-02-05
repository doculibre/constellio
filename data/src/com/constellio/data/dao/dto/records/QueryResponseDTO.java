package com.constellio.data.dao.dto.records;

import java.util.List;
import java.util.Map;

public class QueryResponseDTO {

	private List<RecordDTO> results;

	private Map<RecordDTO, Map<RecordDTO, Double>> resultsWithMoreLikeThis;

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
			List<String> spellCheckerSuggestions, Map<RecordDTO, Map<RecordDTO, Double>> resultsWithMoreLikeThis) {
		this.results = results;
		this.qtime = qtime;
		this.numFound = numFound;
		this.fieldFacetValues = fieldFacetValues;
		this.fieldsStatistics = fieldsStatistics;
		this.queryFacetValues = queryFacetValues;
		this.highlights = highlights;
		this.correctlySpelt = correctlySpelt;
		this.spellCheckerSuggestions = spellCheckerSuggestions;
		this.resultsWithMoreLikeThis = resultsWithMoreLikeThis;
	}

	public List<RecordDTO> getResults() {
		return results;
	}

	public Map<RecordDTO, Map<RecordDTO, Double>> getResultsWithMoreLikeThis() {
		return resultsWithMoreLikeThis;
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
