package com.constellio.model.services.schemas;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecordsModificationBuilder {

	private RecordServices recordServices;

	public RecordsModificationBuilder(RecordServices recordServices) {
		this.recordServices = recordServices;
	}

	public List<RecordsModification> build(Transaction transaction, MetadataSchemaTypes metadataSchemaTypes) {
		List<RecordsModification> recordsModifications = new ArrayList<>();

		Map<String, List<Record>> recordsSplittedByTypes = newRecordUtils().splitRecordsBySchemaTypes(
				transaction.getSavedRecordWithModification());
		for (String schemaTypeCode : metadataSchemaTypes.getSchemaTypesCodesSortedByDependency()) {
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
		List<String> modifiedMetadatasCodes = getModifiedMetadatasDataStoreCodes(records, schemaType);
		for (String metadataDataStoreCode : modifiedMetadatasCodes) {
			modifiedMetadatas.add(schemaType.getMetadataWithDataStoreCode(metadataDataStoreCode));
		}
		return modifiedMetadatas;
	}

	public RecordUtils newRecordUtils() {
		return new RecordUtils();
	}

	public List<String> getModifiedMetadatasDataStoreCodes(List<Record> records, MetadataSchemaType schemaType) {
		Set<String> modifiedMetadatasCodes = new HashSet<>();
		for (Record record : records) {
			RecordImpl recordImpl = (RecordImpl) record;
			RecordImpl originalRecord = (RecordImpl) record.getCopyOfOriginalRecord();
			Map<String, Object> modifiedValues = recordImpl.getModifiedValues();
			modifiedMetadatasCodes.addAll(modifiedValues.keySet());

			recordServices.reloadEagerTransientMetadatas(recordImpl);
			recordServices.reloadEagerTransientMetadatas(originalRecord);
			recordServices.loadLazyTransientMetadatas(recordImpl);
			recordServices.loadLazyTransientMetadatas(originalRecord);

			for (Metadata automaticMetadata : schemaType.getSchema(record.getSchemaCode()).getEagerTransientMetadatas()) {
				Object before = originalRecord.get(automaticMetadata);
				Object after = recordImpl.get(automaticMetadata);
				if (!LangUtils.isEqual(before, after)) {
					modifiedMetadatasCodes.add(automaticMetadata.getDataStoreCode());
				}
			}
			for (Metadata automaticMetadata : schemaType.getSchema(record.getSchemaCode()).getLazyTransientMetadatas()) {
				Object before = originalRecord.get(automaticMetadata);
				Object after = recordImpl.get(automaticMetadata);
				if (!LangUtils.isEqual(before, after)) {
					modifiedMetadatasCodes.add(automaticMetadata.getDataStoreCode());
				}
			}

		}

		return new ArrayList<>(modifiedMetadatasCodes);
	}
}
