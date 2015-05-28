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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BulkImportResults {

	private List<ImportError> importErrors = new ArrayList<>();



	public BulkImportResults() {

	}

	public List<String> getInvalidIds() {
		List<String> invalidIds= new ArrayList<>();
		for(ImportError importError : importErrors){
			invalidIds.add(importError.getInvalidElementId());
		}
		return invalidIds;
	}

	public void add(ImportError importError) {
		this.importErrors.add(importError);
	}

	public List<ImportError> getImportErrors() {
		return Collections.unmodifiableList(importErrors);
	}
}
