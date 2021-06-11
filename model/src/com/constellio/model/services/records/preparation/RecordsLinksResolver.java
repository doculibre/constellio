package com.constellio.model.services.records.preparation;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataNetworkLink;
import com.constellio.model.entities.schemas.MetadataNetworkLinkType;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.AggregatedDataEntry;
import com.constellio.model.entities.schemas.entries.AggregationType;
import com.constellio.model.services.schemas.MetadataList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.entries.AggregationType.REFERENCE_COUNT;
import static com.constellio.model.entities.schemas.entries.DataEntryType.AGGREGATED;
import static java.util.Arrays.asList;

public class RecordsLinksResolver {

	private MetadataSchemaTypes types;

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

		MetadataSchema schema = types.getSchemaOf(record);

		Record originalRecord = record.isSaved() ? record.getCopyOfOriginalRecord() : null;
		Set<String> idsToReindex = new HashSet<>();
		List<AggregatedMetadataIncrementation> aggregatedMetadataIncrementations = new ArrayList<>();
		Set<String> metadatasToReindex = new HashSet<>();
		Set<String> metadatasToIncrement = new HashSet<>();

		MetadataList metadatas = allMetadatas ? schema.getMetadatas() : record.getModifiedMetadatas(types);
		for (Metadata metadata : metadatas) {
			Metadata currentMetadata = metadata.getInheritance() != null ? metadata.getInheritance() : metadata;

			if (!reindexOnly && currentMetadata.getLocalCode().equals(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode())) {
				for (Metadata aMetadata : schema.getMetadatas()) {
					metadatasToIncrement.addAll(getLinkedSumAggregationMetadatasToIncrement(aMetadata, schema));
				}
			} else {
				for (MetadataNetworkLink link : types.getMetadataNetwork().getLinksTo(currentMetadata)) {
					addToReindexedOrIncrementedSet(link, metadatasToReindex, metadatasToIncrement, metadatas,
							record.isSaved(), reindexOnly);
				}
			}
		}

		for (String metadataToIncrement : metadatasToIncrement) {
			boolean added = false;

			List<MetadataNetworkLink> reverseLinks = types.getMetadataNetwork().getLinksFrom(metadataToIncrement);
			for (MetadataNetworkLink reverseLink : reverseLinks) {
				if (reverseLink.getLinkType() == MetadataNetworkLinkType.AGGREGATION_INPUT &&
					isNumberOrIntegerAndNonAggregatedMetadata(reverseLink.getToMetadata()) &&
					isSumAggregationMetadata(reverseLink.getFromMetadata()) &&
					schema.hasMetadataWithCode(reverseLink.getToMetadata().getCode())) {

					Metadata fromMetadata = reverseLink.getFromMetadata();

					// fallback to reindex mode if reference record's metadata is already modified in transaction
					String referenceRecordId = record.get(reverseLink.getRefMetadata());
					if (isRecordMetadataAlreadyModifiedInCurrentTransaction(transaction, referenceRecordId, fromMetadata)) {
						break;
					}
					String originalReferenceRecordId = originalRecord != null ?
													   originalRecord.<String>get(reverseLink.getRefMetadata()) : null;
					boolean deleted = !record.isActive() && (originalRecord != null && originalRecord.isActive());
					boolean restored = record.isActive() && (originalRecord != null && !originalRecord.isActive());

					double current = nullToZero(record.get(reverseLink.getToMetadata()));
					double previous = originalRecord != null ?
									  nullToZero(originalRecord.<Double>get(reverseLink.getToMetadata())) : 0;

					added = addAggregatedMetadataIncrementation(referenceRecordId, originalReferenceRecordId,
							fromMetadata, current, previous, deleted, restored, aggregatedMetadataIncrementations);
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

	private boolean addAggregatedMetadataIncrementation(String referenceRecordId, String originalReferenceRecordId,
														Metadata metadata, double current, double previous,
														boolean deleted, boolean restored,
														List<AggregatedMetadataIncrementation> incrementations) {

		if (referenceRecordId != null && originalReferenceRecordId != null &&
			!referenceRecordId.equals(originalReferenceRecordId)) {
			incrementations.addAll(asList(
					createAggregatedMetadataIncrementation(originalReferenceRecordId, metadata, -current),
					createAggregatedMetadataIncrementation(referenceRecordId, metadata, current)));
			return true;
		} else {
			double delta = calculateDelta(referenceRecordId, originalReferenceRecordId, current, previous, deleted, restored);
			if (delta != 0) {
				String recordId = referenceRecordId != null ? referenceRecordId : originalReferenceRecordId;
				if (recordId != null) {
					incrementations.add(createAggregatedMetadataIncrementation(recordId, metadata, delta));
					return true;
				} else {
					return false;
				}

			}
			return false;
		}
	}

	private void addToReindexedOrIncrementedSet(MetadataNetworkLink link, Set<String> metadatasToReindex,
												Set<String> metadatasToIncrement, MetadataList modifiedMetadatas,
												boolean savedRecord, boolean reindexOnly) {
		if (link.getLevel() > 0 &&
			!metadatasToIncrement.contains(link.getFromMetadata().getCode()) &&
			!metadatasToReindex.contains(link.getFromMetadata().getCode())) {

			if (!link.getToMetadata().isSameLocalCode(link.getRefMetadata())
				|| isReferenceCountAggregation(link.getFromMetadata())
				|| (savedRecord && modifiedMetadatas.containsMetadataWithLocalCode(link.getRefMetadata().getLocalCode()))) {

				if (!reindexOnly && isSumAggregationMetadata(link.getFromMetadata())) {
					metadatasToIncrement.add(link.getFromMetadata().getCode());
				} else {
					metadatasToReindex.add(link.getFromMetadata().getCode());
				}
			}
		}
	}

	private boolean isReferenceCountAggregation(Metadata metadata) {
		return metadata.getDataEntry().getType() == AGGREGATED
			   && ((AggregatedDataEntry) metadata.getDataEntry()).getAgregationType() == REFERENCE_COUNT;

	}

	private boolean isSumAggregationMetadata(Metadata metadata) {
		if (metadata.getDataEntry().getType() == AGGREGATED &&
			((AggregatedDataEntry) metadata.getDataEntry()).getAgregationType() == AggregationType.SUM) {
			List<MetadataNetworkLink> links = types.getMetadataNetwork().getLinksTo(metadata);
			return links.isEmpty();
		}
		return false;
	}

	private boolean isNumberOrIntegerAndNonAggregatedMetadata(Metadata metadata) {
		return (metadata.getType() == NUMBER || metadata.getType() == INTEGER) &&
			   metadata.getDataEntry().getType() != AGGREGATED;
	}

	private boolean isRecordMetadataAlreadyModifiedInCurrentTransaction(Transaction transaction, String recordId,
																		Metadata metadata) {
		if (transaction == null || recordId == null) {
			return false;
		}

		Record recordInTransaction = transaction.getRecord(recordId);
		return (recordInTransaction != null &&
				(!recordInTransaction.isSaved() || recordInTransaction.isModified(metadata)));
	}

	private double calculateDelta(String referenceRecordId, String originalReferenceRecordId,
								  double current, double previous, boolean deleted, boolean restored) {
		if (referenceRecordId == null || deleted) {
			return -current;
		} else if (originalReferenceRecordId == null || restored) {
			return current;
		}
		return current - previous;
	}

	private Collection<String> getLinkedSumAggregationMetadatasToIncrement(Metadata metadata, MetadataSchema schema) {
		Set<String> metadatasToIncrement = new HashSet<>();

		for (MetadataNetworkLink link : types.getMetadataNetwork().getLinksTo(metadata)) {
			if (link.getLevel() > 0 && link.getLinkType() == MetadataNetworkLinkType.AGGREGATION_INPUT &&
				!metadatasToIncrement.contains(link.getFromMetadata().getCode()) &&
				isNumberOrIntegerAndNonAggregatedMetadata(link.getToMetadata()) &&
				isSumAggregationMetadata(link.getFromMetadata()) &&
				schema.hasMetadataWithCode(link.getToMetadata().getCode())) {
				metadatasToIncrement.add(link.getFromMetadata().getCode());
			}
		}
		return metadatasToIncrement;
	}

	private double nullToZero(Number value) {
		return value != null ? value.doubleValue() : 0.0;
	}

	private AggregatedMetadataIncrementation createAggregatedMetadataIncrementation(String recordId, Metadata metadata,
																					double delta) {
		AggregatedMetadataIncrementation incrementation = new AggregatedMetadataIncrementation();
		incrementation.setRecordId(recordId);
		incrementation.setMetadata(metadata);
		incrementation.setAmount(delta);
		return incrementation;
	}
}
