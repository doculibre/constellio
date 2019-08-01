package com.constellio.model.services.schemas;

import com.constellio.model.entities.schemas.ModificationImpact;

import java.util.List;

public class ModificationImpactCalculatorResponse {

	List<ModificationImpact> impacts;

	List<String> recordsToReindexLater;

	public ModificationImpactCalculatorResponse(List<ModificationImpact> impacts, List<String> recordsToReindexLater) {
		this.impacts = impacts;
		this.recordsToReindexLater = recordsToReindexLater;
	}

	public List<ModificationImpact> getImpacts() {
		return impacts;
	}


	public List<String> getRecordsToReindexLater() {
		return recordsToReindexLater;
	}
}