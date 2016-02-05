package com.constellio.sdk.load.script.preparators;

import static com.constellio.model.entities.schemas.Schemas.CODE;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.sdk.load.script.TaxonomyPreparator;

public abstract class BaseTaxonomyPreparator implements TaxonomyPreparator {

	int conceptsPerLevelPerParent = 9;

	int nbLevels = 3;

	public int getConceptsPerLevelPerParent() {
		return conceptsPerLevelPerParent;
	}

	public void setConceptsPerLevelPerParent(int conceptsPerLevelPerParent) {
		this.conceptsPerLevelPerParent = conceptsPerLevelPerParent;
	}

	public int getNbLevels() {
		return nbLevels;
	}

	public void setNbLevels(int nbLevels) {
		this.nbLevels = nbLevels;
	}

	@Override
	public List<RecordWrapper> createRootConcepts(RMSchemasRecordsServices rm) {
		List<RecordWrapper> concepts = new ArrayList<>();
		for (int i = 0; i < conceptsPerLevelPerParent; i++) {
			concepts.add(newConceptWithCodeAndParent(rm, "" + (i + 1), null));
		}
		return concepts;
	}

	protected abstract RecordWrapper newConceptWithCodeAndParent(RMSchemasRecordsServices rm, String code, RecordWrapper parent);

	@Override
	public List<RecordWrapper> createChildConcepts(RMSchemasRecordsServices rm, RecordWrapper parent, Stack<Integer> positions) {

		List<RecordWrapper> concepts = new ArrayList<>();
		int levels = positions.size();

		if (levels < nbLevels) {
			String parentCode = parent.getWrappedRecord().get(CODE);

			for (int i = 0; i < conceptsPerLevelPerParent; i++) {
				String code;
				if (conceptsPerLevelPerParent > 9) {
					code = parentCode + "-" + (i < 10 ? "0" : "") + (i + 1);
				} else {
					code = parentCode + (i + 1);
				}

				concepts.add(newConceptWithCodeAndParent(rm, code, parent));
			}

		}

		return concepts;
	}

}
