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
package com.constellio.sdk.tests;

import java.util.UUID;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.sdk.tests.setups.SchemaShortcuts;

public class TestRecord extends RecordImpl {

	public TestRecord(SchemaShortcuts schema) {
		super(schema.code(), schema.collection(), "TestRecord_" + UUID.randomUUID().toString());
	}

	public TestRecord(SchemaShortcuts schema, String id) {
		super(schema.code(), schema.collection(), id);
	}

	public TestRecord(String schema, String collection, String id) {
		super(schema, collection, id);
	}

	public TestRecord(String schema, String collection) {
		super(schema, collection, "TestRecord_" + UUID.randomUUID().toString());
	}

	public TestRecord(RecordDTO recordDTO) {
		super(recordDTO);
	}

	public TestRecord(MetadataSchema schema, String id) {
		this(schema.getCode(), schema.getCollection(), id);
	}

	public void markAsModified(Metadata metadata) {
		modifiedValues.put(metadata.getDataStoreCode(), get(metadata));
	}

	public TestRecord withTitle(String title) {
		set(Schemas.TITLE, title);
		return this;
	}

}
