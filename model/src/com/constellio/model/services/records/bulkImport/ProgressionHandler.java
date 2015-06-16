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
package com.constellio.model.services.records.bulkImport;

import java.util.HashMap;
import java.util.Map;

public class ProgressionHandler {

	int totalProgression;
	int stepProgression;
	int totalCount = 0;
	Map<String, Integer> counts = new HashMap<>();
	BulkImportProgressionListener listener;

	public ProgressionHandler(BulkImportProgressionListener listener) {
		this.listener = listener;
	}

	public void beforeValidationOfSchema(String schema) {
		listener.updateCurrentStepName("Validating '" + schema + "'");

	}

	public void afterValidationOfSchema(String schema, int recordCount) {
		counts.put(schema, recordCount);
		totalCount += recordCount;
		listener.updateTotal(totalCount);
	}

	public void beforeImportOf(String schema) {
		listener.updateCurrentStepName("Import of '" + schema + "'");
		listener.updateCurrentStepTotal(counts.get(schema));
		this.stepProgression = 0;
	}

	public void incrementProgression() {
		totalProgression++;
		stepProgression++;
		listener.updateProgression(stepProgression, totalProgression);
	}

}
