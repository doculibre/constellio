package com.constellio.model.services.records.preparation;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataNetworkLink;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.entries.AggregatedDataEntry;
import com.constellio.model.entities.schemas.entries.AggregationType;
import com.constellio.model.entities.schemas.entries.DataEntryType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

public class RecordsLinksResolver {

	MetadataSchemaTypes types;

	public RecordsLinksResolver(MetadataSchemaTypes types) {
		this.types = types;
	}

	public void resolveRecordsLinks(Transaction transaction) {

		for (Record record : transaction.getModifiedRecords()) {
			ResolvedRecordLinks resolvedRecordLinks = resolveRecordLinks(transaction, record,
					transaction.getRecordUpdateOptions().isFullRewrite(), false);

			transaction.addAllRecordsToReindex(resolvedRecordLinks.getIdsToReindex());
			transaction.addAggregatedMetadataIncrementations(resolvedRecordLinks.getAggregatedMetadatasToIncrement());
		}
	}

	public Set<String> findRecordsToReindexFromRecord(Record record, boolean allMetadatas) {
		ResolvedRecordLinks resolvedRecordLinks = resolveRecordLinks(null, record, allMetadatas, true);
		return resolvedRecordLinks.getIdsToReindex();
	}

	private ResolvedRecordLinks resolveRecordLinks(Transaction transaction, Record record, boolean allMetadatas,
												   boolean reindexOnly) {

		MetadataSchema schema = types.getSchema(record.getSchemaCode());

		Record originalRecord = record.isSaved() ? record.getCopyOfOriginalRecord() : null;
		Set<String> idsToReindex = new HashSet<>();
		List<AggregatedMetadataIncrementation> aggregatedMetadataIncrementations = new ArrayList<>();
		Set<String> metadatasToReindex = new HashSet<>();
		Set<String> metadatasToIncrement = new HashSet<>();

		List<Metadata> metadatas;

		if (allMetadatas) {
			metadatas = schema.getMetadatas();
		} else {
			metadatas = record.getModifiedMetadatas(types);
		}

		for (Metadata metadata : metadatas) {
			if (metadata.getInheritance() != null) {
				for (MetadataNetworkLink link : types.getMetadataNetwork().getLinksTo(metadata.getInheritance())) {
					addToReindexedOrIncrementedSet(link, metadatasToReindex, metadatasToIncrement, reindexOnly);
				}
			} else {
				for (MetadataNetworkLink link : types.getMetadataNetwork().getLinksTo(metadata)) {
					addToReindexedOrIncrementedSet(link, metadatasToReindex, metadatasToIncrement, reindexOnly);
				}
			}
		}

		for (String metadataToIncrement : metadatasToIncrement) {
			boolean added = false;

			List<MetadataNetworkLink> reverseLinks = types.getMetadataNetwork().getLinksFrom(metadataToIncrement);
			for (MetadataNetworkLink reverseLink : reverseLinks) {
				if (isNumberMetadata(reverseLink.getToMetadata()) &&
					isSumAggregationMetadata(reverseLink.getFromMetadata()) &&
					schema.hasMetadataWithCode(reverseLink.getToMetadata().getCode())) {

					// fallback to reindex mode if reference record is in current transaction
					String referenceRecordId = record.get(reverseLink.getRefMetadata());
					if (isRecordInCurrentTransaction(transaction, referenceRecordId)) {
						break;
					}

					Double current = record.<Double>get(reverseLink.getToMetadata());
					Double previous = originalRecord != null ? originalRecord.<Double>get(reverseLink.getToMetadata()) : null;
					double delta = calculateDelta(current, previous);
					if (delta != 0) {
						AggregatedMetadataIncrementation incrementation = new AggregatedMetadataIncrementation();
						incrementation.setRecordId(referenceRecordId);
						incrementation.setMetadata(reverseLink.getFromMetadata());
						incrementation.setAmount(delta);
						aggregatedMetadataIncrementations.add(incrementation);
						added = true;
					}
					break;
				}
			}

			if (!added) {
				metadatasToReindex.add(metadataToIncrement);
			}
		}


		for (String metadataToReindex : metadatasToReindex) {
			for (MetadataNetworkLink reverseLink : types.getMetadataNetwork().getLinksFrom(metadataToReindex)) {
				Metadata metadata = reverseLink.getToMetadata();
				if (metadata.getType() == REFERENCE && schema.hasMetadataWithCode(metadata.getCode())) {
					if (originalRecord != null) {
						idsToReindex.addAll(originalRecord.<String>getValues(metadata));
					}
					idsToReindex.addAll(record.<String>getValues(metadata));
				}
			}
		}

		return new ResolvedRecordLinks(idsToReindex, aggregatedMetadataIncrementations);
	}

	private void addToReindexedOrIncrementedSet(MetadataNetworkLink link, Set<String> metadatasToReindex,
												Set<String> metadatasToIncrement,
												boolean reindexOnly) {
		if (link.getLevel() > 0 &&
			!metadatasToIncrement.contains(link.getFromMetadata().getCode()) &&
			!metadatasToReindex.contains(link.getFromMetadata().getCode())) {
			if (!reindexOnly && isSumAggregationMetadata(link.getFromMetadata())) {
				metadatasToIncrement.add(link.getFromMetadata().getCode());
			} else {
				metadatasToReindex.add(link.getFromMetadata().getCode());
			}
		}
	}

	private boolean isSumAggregationMetadata(Metadata metadata) {
		if (metadata.getDataEntry().getType() == DataEntryType.AGGREGATED &&
			((AggregatedDataEntry) metadata.getDataEntry()).getAgregationType() == AggregationType.SUM) {
			List<MetadataNetworkLink> links = types.getMetadataNetwork().getLinksTo(metadata);
			return links.isEmpty();
		}
		return false;
	}

	private boolean isNumberMetadata(Metadata metadata) {
		return metadata.getType() == NUMBER;
	}

	private boolean isRecordInCurrentTransaction(Transaction transaction, String referenceRecordId) {
		if (transaction == null || referenceRecordId == null) {
			return false;
		}

		Record recordInTransaction = transaction.getRecord(referenceRecordId);
		return (recordInTransaction != null && !recordInTransaction.isSaved());
	}

	private double calculateDelta(Double current, Double previous) {
		return (current != null ? current : 0.0) - (previous != null ? previous : 0.0);
	}
}
