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
package com.constellio.app.services.schemas.bulkImport.data.builder;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.services.schemas.bulkImport.data.ImportData;

public class ImportDataBuilder {

	private String schema;

	private String id;

	private Map<String, Object> fields = new HashMap<>();

	public ImportDataBuilder setSchema(String schema) {
		this.schema = schema;
		return this;
	}

	public ImportDataBuilder setId(String id) {
		this.id = id;
		return this;
	}

	public ImportDataBuilder addField(String key, Object value) {
		this.fields.put(key, value);
		return this;
	}

	public ImportData build(int index) {
		return new ImportData(index, schema, id, fields);
	}
}
