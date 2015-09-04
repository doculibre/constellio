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
package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordModificationEvent implements RecordEvent {

	MetadataList modifiedMetadatas;

	Record record;

	public RecordModificationEvent(Record record, MetadataList modifiedMetadatas) {
		this.record = record;
		this.modifiedMetadatas = modifiedMetadatas;
	}

	public Record getRecord() {
		return record;
	}

	public MetadataList getModifiedMetadatas() {
		return modifiedMetadatas;
	}

	public boolean hasModifiedMetadata(String metadataLocalCode) {
		for (String code : modifiedMetadatas.toMetadatasCodesList()) {
			if (code.endsWith(metadataLocalCode)) {
				return true;
			}
		}
		return false;
	}

	public boolean isSchemaType(String schemaType) {
		return schemaType.equals(getSchemaTypeCode());
	}

	public String getSchemaTypeCode() {
		return new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
	}
}
