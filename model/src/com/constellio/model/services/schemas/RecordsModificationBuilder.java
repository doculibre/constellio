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
