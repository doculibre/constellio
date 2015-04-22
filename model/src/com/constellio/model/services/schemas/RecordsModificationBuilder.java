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
import java.util.Map;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordUtils;

public class RecordsModificationBuilder {

	public List<RecordsModification> build(Transaction transaction, MetadataSchemaTypes metadataSchemaTypes) {
		List<RecordsModification> recordsModifications = new ArrayList<>();
		Map<String, List<Record>> recordsSplittedByTypes = newRecordUtils()
				.splitRecordsBySchemaTypes(transaction.getSavedRecordWithModification());
		for (String schemaTypeCode : metadataSchemaTypes.getSchemaTypesSortedByDependency()) {
			if (recordsSplittedByTypes.containsKey(schemaTypeCode)) {
				MetadataSchemaType schemaType = metadataSchemaTypes.getSchemaType(schemaTypeCode);
				List<Record> records = recordsSplittedByTypes.get(schemaTypeCode);
				recordsModifications.add(getSchemaTypeRecordsModifications(schemaType, records));
			}
		}
		return recordsModifications;
	}

	private RecordsModification getSchemaTypeRecordsModifications(MetadataSchemaType schemaType, List<Record> records) {
		List<Metadata> modifiedMetadatas = getModifiedMetadatas(schemaType, records);
		return new RecordsModification(records, modifiedMetadatas, schemaType);
	}

	private List<Metadata> getModifiedMetadatas(MetadataSchemaType schemaType, List<Record> records) {
		List<Metadata> modifiedMetadatas = new ArrayList<>();
		List<String> modifiedMetadatasCodes = newRecordUtils().getModifiedMetadatasDataStoreCodes(records);
		for (String metadataDataStoreCode : modifiedMetadatasCodes) {
			modifiedMetadatas.add(schemaType.getMetadataWithDataStoreCode(metadataDataStoreCode));
		}
		return modifiedMetadatas;
	}

	public RecordUtils newRecordUtils() {
		return new RecordUtils();
	}
}
