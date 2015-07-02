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
package com.constellio.app.services.schemas.bulkImport;

import java.util.HashMap;
import java.util.Map;

import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.app.services.schemas.bulkImport.data.ImportData;

public class ImportDataErrors {

	ValidationErrors errors;

	String schemaType;

	ImportData importData;

	public ImportDataErrors(String schemaType, ValidationErrors errors, ImportData importData) {
		this.errors = errors;
		this.importData = importData;
		this.schemaType = schemaType;
	}

	public void error(String code, Map<String, String> parameters) {
		parameters.put("index", "" + (importData.getIndex() + 1));
		parameters.put("legacyId", importData.getLegacyId());
		parameters.put("schemaType", schemaType);
		errors.add(RecordsImportServices.class, code, parameters);
	}

	public void error(String code) {
		HashMap<String, String> parameters = new HashMap<>();
		parameters.put("index", "" + (importData.getIndex() + 1));
		parameters.put("legacyId", importData.getLegacyId());
		parameters.put("schemaType", schemaType);
		errors.add(RecordsImportServices.class, code, parameters);
	}
}
