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
package com.constellio.model.services.schemas;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public class RecordsModification {

	private MetadataSchemaType metadataSchemaType;

	private List<Record> records = new ArrayList<>();

	private List<Metadata> modifiedMetadatas = new ArrayList<>();

	public RecordsModification(List<Record> records,
			List<Metadata> modifiedMetadatas, MetadataSchemaType metadataSchemaType) {
		this.records = records;
		this.modifiedMetadatas = modifiedMetadatas;
		this.metadataSchemaType = metadataSchemaType;
	}

	public List<Record> getRecords() {
		return records;
	}

	public List<Metadata> getModifiedMetadatas() {
		return modifiedMetadatas;
	}

	public MetadataSchemaType getMetadataSchemaType() {
		return metadataSchemaType;
	}
}
