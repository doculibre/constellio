package com.constellio.app.services.records;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.formula.functions.T;
import org.joda.time.LocalDateTime;

import com.constellio.model.frameworks.validation.ValidationErrors;

public class SystemCheckResults {

	LocalDateTime dateTime;

	//	int logicallyDeletedUsers = 0;
	//	int checkedReferences = 0;
	//	int brokenReferences = 0;

	Set<String> repairedRecords = new HashSet<>();

	Map<String, Integer> metrics = new HashMap<>();

	Map<String, Object> resultsInfos = new HashMap<>();

	ValidationErrors errors = new ValidationErrors();

	public SystemCheckResults(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public ValidationErrors getErrors() {
		return errors;
	}

	public void markAsRepaired(String id) {
		repairedRecords.add(id);
	}

	public Map<String, Integer> getMetrics() {
		return metrics;
	}

	public Map<String, Object> getResultsInfos() {
		return resultsInfos;
	}

	public Integer getMetric(String key) {
		return metrics.containsKey(key) ? metrics.get(key) : 0;
	}

	public Set<String> getRepairedRecords() {
		return repairedRecords;
	}
}

