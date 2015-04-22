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

import java.util.Map;

public class ImportRecord {
	private final String collection;
	private final String schemaType;
	private final String previousSystemId;
	private final Map<String, Object> fields;

	public ImportRecord(String collection, String schemaType, String previousSystemId, Map<String, Object> fields) {
		this.collection = collection;
		this.schemaType = schemaType;
		this.previousSystemId = previousSystemId;
		this.fields = fields;
	}

	public String getCollection() {
		return collection;
	}

	public String getSchemaType() {
		return schemaType;
	}

	public String getPreviousSystemId() {
		return previousSystemId;
	}

	public Map<String, Object> getFields() {
		return fields;
	}
}
