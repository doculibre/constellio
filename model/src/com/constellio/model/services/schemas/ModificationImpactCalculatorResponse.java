package com.constellio.model.services.schemas;

import com.constellio.model.entities.schemas.ModificationImpact;

import java.util.List;

public class ModificationImpactCalculatorResponse {

	List<ModificationImpact> impacts;

	public ModificationImpactCalculatorResponse(List<ModificationImpact> impacts) {
		this.impacts = impacts;
	}

	public List<ModificationImpact> getImpacts() {
		return impacts;
	}

}