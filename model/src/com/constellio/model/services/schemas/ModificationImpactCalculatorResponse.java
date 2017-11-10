package com.constellio.model.services.schemas;

import java.util.List;

import com.constellio.model.entities.schemas.ModificationImpact;

public class ModificationImpactCalculatorResponse {

	List<ModificationImpact> impacts;

	List<String> recordsToReindex;

	public ModificationImpactCalculatorResponse(List<ModificationImpact> impacts, List<String> recordsToReindex) {
		this.impacts = impacts;
		this.recordsToReindex = recordsToReindex;
	}

	public List<ModificationImpact> getImpacts() {
		return impacts;
	}

	public List<String> getRecordsToReindex() {
		return recordsToReindex;
	}
}