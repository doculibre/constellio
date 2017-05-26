package com.constellio.model.services.records.preparation;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataNetworkLink;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

public class RecordsToReindexResolver {

	MetadataSchemaTypes types;

	public RecordsToReindexResolver(MetadataSchemaTypes types) {
		this.types = types;
	}

	public void findRecordsToReindex(Transaction transaction) {

		for (Record record : transaction.getModifiedRecords()) {
			transaction.addAllRecordsToReindex(
					findRecordsToReindexFromRecord(record, transaction.getRecordUpdateOptions().isFullRewrite()));
		}

	}

	public Set<String> findRecordsToReindexFromRecord(Record record, boolean allMetadatas) {

		MetadataSchema schema = types.getSchema(record.getSchemaCode());

		Record originalRecord = record.isSaved() ? record.getCopyOfOriginalRecord() : null;
		Set<String> ids = new HashSet<>();
		Set<String> metadatasToReindex = new HashSet<>();

		List<Metadata> metadatas;

		if (allMetadatas) {
			metadatas = schema.getMetadatas();
		} else {
			metadatas = record.getModifiedMetadatas(types);
		}

		for (Metadata metadata : metadatas) {
			for (MetadataNetworkLink link : types.getMetadataNetwork().getLinksTo(metadata)) {
				if (link.getLevel() > 0) {
					metadatasToReindex.add(link.getFromMetadata().getCode());
				}
			}
		}

		for (String metadataToReindex : metadatasToReindex) {
			for (MetadataNetworkLink reverseLink : types.getMetadataNetwork().getLinksFrom(metadataToReindex)) {
				Metadata metadata = reverseLink.getToMetadata();
				if (metadata.getType() == REFERENCE && schema.hasMetadataWithCode(metadata.getCode())) {

					if (originalRecord != null) {
						String wasValue = originalRecord.get(metadata);
						if (wasValue != null) {
							ids.add(wasValue);
						}
					}

					if (record != null) {
						if(metadata.isMultivalue()) {
							List<String> value = record.getList(metadata);
							ids.addAll(value);
						} else {
							String value = record.get(metadata);
							if (value != null) {
								ids.add(value);
							}
						}
					}

				}
			}
		}

		return ids;
	}

}
