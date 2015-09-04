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
