package com.constellio.model.services.schemas;

import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.dao.services.records.DataStore;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.KeyIntMap;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.records.ImpactHandlingMode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordAuthorization;
import com.constellio.model.entities.schemas.HierarchyReindexingRecordsModificationImpact;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataNetworkLink;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.entities.schemas.ModificationImpact.ModificationImpactDetail;
import com.constellio.model.entities.schemas.QueryBasedReindexingBatchProcessModificationImpact;
import com.constellio.model.entities.schemas.ReindexingRecordsModificationImpact;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.CacheBasedTaxonomyVisitingServices;
import com.constellio.model.services.taxonomies.CacheBasedTaxonomyVisitingServicesException.CacheBasedTaxonomyVisitingServicesException_NotAvailableCacheNotLoaded;
import com.constellio.model.utils.Lazy;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.constellio.model.entities.records.ImpactHandlingMode.HANDLE_ALL_IN_TRANSACTION_OR_EXCEPTION;
import static com.constellio.model.services.schemas.SchemaUtils.areCacheIndex;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.ALL_REMOVED_AUTHS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.ATTACHED_ANCESTORS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.TOKENS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasInCollectionOf;
import static com.constellio.model.services.taxonomies.TaxonomyVisitingStatus.CONTINUE;
import static com.constellio.model.services.taxonomies.TaxonomyVisitingStatus.STOP;
import static java.util.Arrays.asList;

//AFTER : Move in com.constellio.model.services.records.
public class ModificationImpactCalculator {

	public static int MINIMUM_HIERARCHY_SIZE_TO_BE_CONSIDERED_CASCADING = 250;
	public static int MINIMUM_HIERARCHY_LEVELS_TO_BE_CONSIDERED_CASCADING = 2;

	private static final Logger LOGGER = LoggerFactory.getLogger(ModificationImpactCalculator.class);

	SearchServices searchServices;

	RecordServices recordServices;

	List<Taxonomy> taxonomies;

	MetadataSchemaTypes metadataSchemaTypes;

	SchemaUtils schemaUtils;

	CacheBasedTaxonomyVisitingServices visitingServices;

	public ModificationImpactCalculator(MetadataSchemaTypes metadataSchemaTypes, List<Taxonomy> taxonomies,
										SearchServices searchServices, RecordServices recordServices,
										CacheBasedTaxonomyVisitingServices visitingServices) {
		this(metadataSchemaTypes, taxonomies, searchServices, recordServices, visitingServices, new SchemaUtils());
	}

	public ModificationImpactCalculator(MetadataSchemaTypes metadataSchemaTypes, List<Taxonomy> taxonomies,
										SearchServices searchServices, RecordServices recordServices,
										CacheBasedTaxonomyVisitingServices visitingServices,
										SchemaUtils schemaUtils) {
		this.taxonomies = taxonomies;
		this.searchServices = searchServices;
		this.recordServices = recordServices;
		this.metadataSchemaTypes = metadataSchemaTypes;
		this.schemaUtils = schemaUtils;
		this.visitingServices = visitingServices;
	}

	public ModificationImpactCalculatorResponse findTransactionImpact(Transaction transaction) {

		List<ModificationImpact> recordsModificationImpacts = new ArrayList<>();
		ImpactHandlingMode mode = transaction.getRecordUpdateOptions().getImpactHandlingMode();
		if (mode != ImpactHandlingMode.NEXT_SYSTEM_REINDEXING) {
			List<RecordsModification> recordsModifications = new RecordsModificationBuilder(recordServices)
					.build(transaction, metadataSchemaTypes);

			List<String> transactionRecordIds = mode == ImpactHandlingMode.DELEGATED ? null : transaction.getRecordIds();

			Set<RecordId> reindexedHierarchyRecordIds = new HashSet<>();

			for (RecordsModification recordsModification : recordsModifications) {
				for (ModificationImpact modificationImpact : findImpactOfARecordsModification(
						recordsModification, transactionRecordIds, transaction.getTitle())) {

					if (modificationImpact instanceof HierarchyReindexingRecordsModificationImpact) {
						RecordId id = ((HierarchyReindexingRecordsModificationImpact) modificationImpact).getRootIdToReindex();
						if (!reindexedHierarchyRecordIds.contains(id)) {
							reindexedHierarchyRecordIds.add(id);
							recordsModificationImpacts.add(modificationImpact);
						}
					} else {
						recordsModificationImpacts.add(modificationImpact);
					}

				}

			}

			//			Set<RecordId> idsToReindexDueToOtherReason = new HashSet<>();
			//			Iterator<ModificationImpact> collectedModificationImpactIterator = recordsModificationImpacts.iterator();
			//			while(collectedModificationImpactIterator.hasNext()) {
			//				ModificationImpact impact = collectedModificationImpactIterator.next();
			//				if (impact instanceof HierarchyReindexingRecordsModificationImpact) {
			//					idsToReindexDueToOtherReason.addAll(impact.get)
			//
			//				}
			//			}

			for (Record record : transaction.getRecords()) {

				if (RecordAuthorization.SCHEMA_TYPE.equals(record.getTypeCode())) {
					MetadataSchema authSchema = metadataSchemaTypes.getDefaultSchema(RecordAuthorization.SCHEMA_TYPE);
					Metadata authorizationPrincipals = authSchema.getMetadata(RecordAuthorization.PRINCIPALS);
					Metadata lastTokenRecalculate = authSchema.getMetadata(RecordAuthorization.LAST_TOKEN_RECALCULATE);
					Metadata authorizationTargetMetadata = authSchema.getMetadata(RecordAuthorization.TARGET);
					Metadata authorizationTargetSchemaTypeMetadata = authSchema.getMetadata(RecordAuthorization.TARGET_SCHEMA_TYPE);

					String authorizationTarget = record.get(authorizationTargetMetadata);
					String authorizationTargetSchemaType = record.get(authorizationTargetSchemaTypeMetadata);

					if (RecordAuthorization.isSecurableSchemaType(authorizationTargetSchemaType)
						&& (record.isModified(authorizationPrincipals) || record.isModified(lastTokenRecalculate))) {

						if (isAuthorizationOnTargetCascading(authorizationTarget)) {
							if (!reindexedHierarchyRecordIds.contains(RecordId.id(authorizationTarget))) {
								reindexedHierarchyRecordIds.add(RecordId.id(authorizationTarget));
								recordsModificationImpacts.add(new HierarchyReindexingRecordsModificationImpact(
										record.getCollection(),
										RecordId.id(authorizationTarget),
										new Lazy<List<ModificationImpactDetail>>() {
											@Override
											protected List<ModificationImpactDetail> load() {
												return getCascadingImpactDetails(record.getCollection(), RecordId.id(authorizationTarget));
											}
										}));
							}
						} else {
							LogicalSearchCondition condition = fromAllSchemasInCollectionOf(record, DataStore.RECORDS)
									.where(Schemas.ATTACHED_ANCESTORS).isEqualTo(authorizationTarget);
							transaction.addAllRecordsToReindex(searchServices.searchRecordIds(condition));
						}
					}

				}

			}
		}
		return new ModificationImpactCalculatorResponse(recordsModificationImpacts);
	}

	private boolean isAuthorizationOnTargetCascading(String authorizationTarget) {
		return false;
	}

	List<ModificationImpact> findImpactOfARecordsModification(RecordsModification recordsModification,
															  List<String> transactionRecordIds,
															  String transactionTitle) {

		List<ModificationImpact> recordsModificationImpactsInType = new ArrayList<>();

		List<PotentialImpact> potentialImpacts = findPotentialMetadataToReindexAndTheirReferencesToAModifiedMetadata(recordsModification);

		return findRealImpactsOfPotentialMetadataToReindex(recordsModification, transactionRecordIds,
				recordsModificationImpactsInType, potentialImpacts, transactionTitle);
	}

	private KeyIntMap<String> countImpactsOnSchemaTypes(LogicalSearchCondition condition) {
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.setNumberOfRows(0);
		query.addFieldFacet(Schemas.SCHEMA.getDataStoreCode());
		query.setName("Count impacts on schema types");

		SPEQueryResponse response = searchServices.query(query);

		KeyIntMap<String> counts = new KeyIntMap<>();
		for (FacetValue facetValue : response.getFieldFacetValues(Schemas.SCHEMA.getDataStoreCode())) {
			String schemaTypeCode = SchemaUtils.getSchemaTypeCode(facetValue.getValue());
			counts.increment(schemaTypeCode, (int) facetValue.getQuantity());
		}

		return counts;

	}

	private LogicalSearchCondition getLogicalSearchConditionFor(MetadataSchemaType schemaType,
																List<Record> modifiedRecordsBatch,
																List<String> transactionRecordIds,
																List<Metadata> references) {
		LogicalSearchCondition condition = from(schemaType).whereAny(references).isIn(modifiedRecordsBatch);
		if (transactionRecordIds != null) {
			condition = condition.andWhere(Schemas.IDENTIFIER).isNotIn(transactionRecordIds);
		}

		return condition;
	}

	private List<ModificationImpact> findRealImpactsOfPotentialMetadataToReindex(
			RecordsModification recordsModification,
			List<String> transactionRecordIds,
			List<ModificationImpact> recordsModificationImpactsInType,
			List<PotentialImpact> potentialImpacts,
			String transactionTitle) {


		if (!potentialImpacts.isEmpty()) {
			boolean handledNow = recordsModification.getOptions().getImpactHandlingMode() == HANDLE_ALL_IN_TRANSACTION_OR_EXCEPTION;
			Iterator<List<Record>> batchIterator = splitModifiedRecordsInBatchOf1000(recordsModification);
			while (batchIterator.hasNext()) {
				List<Record> modifiedRecordsBatch = batchIterator.next();

				if (!handledNow && isCascading(recordsModification)) {
					List<RecordId> recordIds = modifiedRecordsBatch.stream().map(Record::getRecordId).collect(Collectors.toList());
					for (RecordId recordId : recordIds) {
						recordsModificationImpactsInType.add(new HierarchyReindexingRecordsModificationImpact(
								recordsModification.getMetadataSchemaType().getCollection(), recordId, new Lazy<List<ModificationImpactDetail>>() {
							@Override
							protected List<ModificationImpactDetail> load() {
								return getCascadingImpactDetails(recordsModification.getMetadataSchemaType().getCollection(), recordId);

							}
						}));
					}
				} else {
					int recordsCount;
					for (PotentialImpact potentialImpact : potentialImpacts) {
						LogicalSearchCondition condition = getLogicalSearchConditionFor(potentialImpact.schemaType, modifiedRecordsBatch,
								transactionRecordIds, potentialImpact.references);
						if (potentialImpact.schemaType.getCacheType().hasPermanentCache() && areCacheIndex(potentialImpact.references)) {

							Set<String> ids = new HashSet<>();
							for (Metadata referenceMetadata : potentialImpact.references) {
								for (Record modifiedRecord : modifiedRecordsBatch) {
									for (String id : recordServices.getRecordsCaches()
											.getRecordsByIndexedMetadata(potentialImpact.schemaType, referenceMetadata, modifiedRecord.getId())
											.map(Record::getId).collect(Collectors.toList())) {

										if (transactionRecordIds == null || !transactionRecordIds.contains(id)) {
											ids.add(id);
										}
									}
								}
							}
							recordsCount = ids.size();
							if (recordsCount > 0) {
								Lazy<List<String>> idsSupplier = new Lazy<List<String>>() {
									@Override
									protected List<String> load() {
										return new ArrayList<>(ids);
									}
								};
								Lazy<List<Record>> recordsSupplier = new Lazy<List<Record>>() {
									@Override
									protected List<Record> load() {
										return recordServices.get(new ArrayList<>(ids));
									}
								};
								recordsModificationImpactsInType.add(new ReindexingRecordsModificationImpact(
										potentialImpact.schemaType, idsSupplier, recordsSupplier, potentialImpact.reindexedMetadatas, handledNow));
							}

						} else {
							recordsCount = (int) searchServices.getResultsCount(condition);

							if (recordsCount > 0) {
								recordsModificationImpactsInType.add(new QueryBasedReindexingBatchProcessModificationImpact(
										potentialImpact.schemaType, potentialImpact.reindexedMetadatas, condition, recordsCount, transactionTitle, handledNow));
							}
						}

					}
				}
			}
		}

		return recordsModificationImpactsInType;
	}

	List<PotentialImpact> findPotentialMetadataToReindexAndTheirReferencesToAModifiedMetadata(
			RecordsModification recordsModification) {

		List<PotentialImpact> potentialImpacts = new ArrayList<>();

		for (MetadataSchemaType schemaType : metadataSchemaTypes.getSchemaTypes()) {


			List<Metadata> reindexedMetadatas = new ArrayList<>();
			List<Metadata> references = new ArrayList<>();
			Set<String> reindexedMetadatasCodes = new HashSet<>();
			Set<String> referencesCodes = new HashSet<>();

			List<Metadata> automaticMetadatas = schemaType.getAutomaticMetadatas();
			List<Metadata> modifiedMetadatas = recordsModification.getModifiedMetadatas();

			for (Metadata automaticMetadata : automaticMetadatas) {
				List<Metadata> referenceMetadatasLinkingToModifiedMetadatas = getReferenceMetadatasLinkingToModifiedMetadatas(
						automaticMetadata, modifiedMetadatas);
				if (!referenceMetadatasLinkingToModifiedMetadatas.isEmpty()) {

					if (!reindexedMetadatasCodes.contains(automaticMetadata.getLocalCode())) {
						reindexedMetadatasCodes.add(automaticMetadata.getLocalCode());
						reindexedMetadatas.add(automaticMetadata);
					}

					for (Metadata aReference : referenceMetadatasLinkingToModifiedMetadatas) {
						if (!referencesCodes.contains(aReference.getLocalCode())) {
							referencesCodes.add(aReference.getLocalCode());
							references.add(aReference);
						}
					}
				}
			}

			if (!references.isEmpty()) {
				potentialImpacts.add(new PotentialImpact(schemaType, references, reindexedMetadatas));
			}
		}

		return potentialImpacts;
	}

	@AllArgsConstructor
	static class PotentialImpact {
		MetadataSchemaType schemaType;
		List<Metadata> references;
		List<Metadata> reindexedMetadatas;
	}

	List<Metadata> getReferenceMetadatasLinkingToModifiedMetadatas(Metadata automaticMetadata,
																   List<Metadata> modifiedMetadatas) {
		MetadataList returnedReferences = new MetadataList();
		for (Metadata modifiedMetadata : modifiedMetadatas) {
			returnedReferences.addAll(getReferencesToMetadata(automaticMetadata, modifiedMetadata));
		}
		return returnedReferences;
	}

	List<Metadata> getReferencesToMetadata(Metadata automaticMetadata, Metadata modifiedMetadata) {
		List<Metadata> referencesToMetadata;

		if (automaticMetadata.getDataEntry().getType() == DataEntryType.COPIED) {
			if (isCopiedMetadataReferencingTheGivenModifiedMetadata(automaticMetadata, modifiedMetadata)) {
				referencesToMetadata = asList(getReferenceMetadataUsedByCopiedMetadata(automaticMetadata));
			} else {
				referencesToMetadata = Collections.emptyList();
			}
		} else if (automaticMetadata.getDataEntry().getType() == DataEntryType.CALCULATED) {
			referencesToMetadata = getReferenceMetadatasUsedByTheGivenCalculatedMetadataToObtainValuesOfTheModifiedMetadata(
					automaticMetadata, modifiedMetadata);

		} else if (automaticMetadata.getDataEntry().getType() == DataEntryType.AGGREGATED) {
			referencesToMetadata = Collections.emptyList();

		} else {
			throw new ImpossibleRuntimeException("Unsupported type : " + automaticMetadata.getDataEntry().getType());
		}

		return referencesToMetadata;
	}

	private boolean isCopiedMetadataReferencingTheGivenModifiedMetadata(Metadata automaticMetadata,
																		Metadata modifiedMetadata) {

		CopiedDataEntry dataEntry = (CopiedDataEntry) automaticMetadata.getDataEntry();
		String modifiedMetadataCode = modifiedMetadata.getCode();
		String metadataCopiedToAutomaticMetadataCode = dataEntry.getCopiedMetadata();
		return modifiedMetadataCode.equals(metadataCopiedToAutomaticMetadataCode);
	}

	private Metadata getReferenceMetadataUsedByCopiedMetadata(Metadata copiedMetadata) {
		CopiedDataEntry copiedDataEntry = (CopiedDataEntry) copiedMetadata.getDataEntry();
		return metadataSchemaTypes.getMetadata(copiedDataEntry.getReferenceMetadata());
	}

	private List<Metadata> getReferenceMetadatasUsedByTheGivenCalculatedMetadataToObtainValuesOfTheModifiedMetadata(
			Metadata automaticMetadata, Metadata modifiedMetadata) {

		List<Metadata> referencesToMetadata = new ArrayList<>();
		CalculatedDataEntry dataEntry = (CalculatedDataEntry) automaticMetadata.getDataEntry();

		for (Dependency dependency : dataEntry.getCalculator().getDependencies()) {
			referencesToMetadata.addAll(
					addReferenceMetadatasUsedByTheGivenDependencyToObtainValuesOfTheModifiedMetadata(automaticMetadata,
							modifiedMetadata, dependency));
		}
		return referencesToMetadata;
	}

	private List<Metadata> addReferenceMetadatasUsedByTheGivenDependencyToObtainValuesOfTheModifiedMetadata(
			Metadata automaticMetadata,
			Metadata modifiedMetadata, Dependency dependency) {
		if (dependency instanceof ReferenceDependency) {
			return getReferenceMetadatasUsedByTheGivenReferenceDependencyToObtainValuesOfTheModifiedMetadata(
					automaticMetadata, modifiedMetadata, (ReferenceDependency) dependency);

		} else if (SpecialDependencies.HIERARCHY.equals(dependency)
				   && modifiedMetadataHasPotentialHierarchyImpactOnAutomaticMetadata(automaticMetadata, modifiedMetadata)) {
			return getReferenceMetadatasUsedByTheGivenHierarchyDependencyToObtainValuesOfTheModifiedMetadata(
					automaticMetadata, modifiedMetadata);

		} else {
			return Collections.emptyList();
		}
	}

	private boolean modifiedMetadataHasPotentialHierarchyImpactOnAutomaticMetadata(Metadata automaticMeta,
																				   Metadata modifiedMeta) {

		return modifiedMeta.isLocalCode(CommonMetadataBuilder.PATH)
			   || (modifiedMeta.isLocalCode(TOKENS) && automaticMeta.isLocalCode(TOKENS))
			   || (modifiedMeta.isLocalCode(TOKENS) && automaticMeta.isLocalCode(TOKENS))
			   || (modifiedMeta.isLocalCode(ATTACHED_ANCESTORS) && automaticMeta.isLocalCode(ATTACHED_ANCESTORS))
			   || (modifiedMeta.isLocalCode(ALL_REMOVED_AUTHS) && automaticMeta.isLocalCode(ALL_REMOVED_AUTHS));
	}

	private List<Metadata> getReferenceMetadatasUsedByTheGivenReferenceDependencyToObtainValuesOfTheModifiedMetadata(
			Metadata automaticMetadata, Metadata modifiedMetadata,
			ReferenceDependency<?> referenceDependency) {

		List<Metadata> referencesToMetadata = new ArrayList<>();
		if (isDependencyReferencingAnySchemaMetadataWithCode(referenceDependency, modifiedMetadata.getLocalCode())) {
			Metadata reference = getDependencyReferenceMetadata(automaticMetadata, referenceDependency);
			String dependencyCode = schemaUtils.getDependencyCode(referenceDependency, reference);
			if (dependencyCode.equals(modifiedMetadata.getCode())) {
				referencesToMetadata.add(reference);
			}
		}
		return referencesToMetadata;
	}

	private List<Metadata> getReferenceMetadatasUsedByTheGivenHierarchyDependencyToObtainValuesOfTheModifiedMetadata(
			Metadata automaticMetadata, Metadata modifiedMetadata) {
		MetadataList referencesToMetadata = new MetadataList();
		String modifiedMetadataSchemaTypeCode = schemaUtils
				.getSchemaTypeCode(schemaUtils.getSchemaCode(modifiedMetadata));
		MetadataSchemaType automaticMetadataSchema = metadataSchemaTypes
				.getSchemaType(schemaUtils.getSchemaTypeCode(automaticMetadata));

		referencesToMetadata.addAll(automaticMetadataSchema.getTaxonomySchemasMetadataWithChildOfRelationship(taxonomies));
		referencesToMetadata.addAll(automaticMetadataSchema.getAllReferencesToTaxonomySchemas(taxonomies));
		referencesToMetadata.addAll(automaticMetadataSchema.getAllParentReferences());
		return referencesToMetadata.onlyReferencesToType(modifiedMetadataSchemaTypeCode);
	}

	private Metadata getDependencyReferenceMetadata(Metadata automaticMetadata,
													ReferenceDependency<?> referenceDependency) {
		String referenceCode = schemaUtils.getReferenceCode(automaticMetadata, referenceDependency);
		return metadataSchemaTypes.getMetadata(referenceCode);
	}

	boolean isDependencyReferencingAnySchemaMetadataWithCode(ReferenceDependency<?> referenceDependency,
															 String metadataCode) {

		String metadataLocalCode = new SchemaUtils().toLocalMetadataCode(metadataCode);

		return referenceDependency.getDependentMetadataCode().contains(metadataLocalCode);
	}

	private Iterator<List<Record>> splitModifiedRecordsInBatchOf1000(RecordsModification recordsModification) {
		return new BatchBuilderIterator<>(recordsModification.getRecords().iterator(), 100);
	}

	public boolean isCascading(RecordsModification recordsModification) {

		Set<String> classifiedSchemaTypes = recordsModification.getMetadataSchemaType().getSchemaTypes()
				.getClassifiedSchemaTypesIncludingSelfIn(recordsModification.getMetadataSchemaType().getCode())
				.stream().map(MetadataSchemaType::getCode).collect(Collectors.toSet());
		boolean possibleCascading = false;
		for (Metadata modifiedMetadata : recordsModification.getModifiedMetadatas()) {
			for (MetadataNetworkLink link : recordsModification.getMetadataSchemaType().getSchemaTypes()
					.getMetadataNetwork().getLinksTo(modifiedMetadata)) {
				if (classifiedSchemaTypes.contains(link.getFromMetadata().getSchemaType().getCode())) {
					possibleCascading = true;
				}
			}
		}

		if (!possibleCascading) {
			return false;
		}

		/**
		 * For the moment, we consider a modification to be cascading if it affect records of levels or more
		 */
		AtomicBoolean cascading = new AtomicBoolean();
		AtomicInteger maxVisitedDepth = new AtomicInteger();
		AtomicInteger maxVisitedRecords = new AtomicInteger();
		for (Record record : recordsModification.getRecords()) {
			if (!cascading.get()) {
				try {
					visitingServices.visit(record, (visited) -> {
						int newMaxVisitedDepth = Math.max(maxVisitedDepth.intValue(), visited.getLevel());
						maxVisitedDepth.set(newMaxVisitedDepth);
						int newVisitedRecordsCount = maxVisitedRecords.incrementAndGet();
						if (newMaxVisitedDepth >= MINIMUM_HIERARCHY_LEVELS_TO_BE_CONSIDERED_CASCADING

							&& newVisitedRecordsCount >= MINIMUM_HIERARCHY_SIZE_TO_BE_CONSIDERED_CASCADING) {
							cascading.set(true);
							return STOP;
						}

						return CONTINUE;
					});
				} catch (CacheBasedTaxonomyVisitingServicesException_NotAvailableCacheNotLoaded ignored) {
					//Already validated
					return true;
				}

			}
		}


		return cascading.get();

		//		return false;
	}

	private List<ModificationImpactDetail> getCascadingImpactDetails(String collection, RecordId recordId) {

		LogicalSearchQuery query = new LogicalSearchQuery();

		query.setCondition(fromAllSchemasIn(collection)
				.where(Schemas.PATH_PARTS).isEqualTo(recordId.stringValue()));

		query.setNumberOfRows(0);
		query.addFieldFacet(Schemas.SCHEMA.getDataStoreCode());


		SPEQueryResponse response = searchServices.query(query);

		KeyIntMap<String> agregatedCounts = new KeyIntMap<>();
		response.getFieldFacetValues(Schemas.SCHEMA.getDataStoreCode()).forEach(v -> {
			String schemaType = SchemaUtils.getSchemaTypeCode(v.getValue());
			agregatedCounts.increment(schemaType, (int) v.getQuantity());
		});


		return agregatedCounts.entriesSortedByDescValue().stream()
				.map(e -> new ModificationImpactDetail(metadataSchemaTypes.getSchemaType(e.getKey()), e.getValue()))
				.collect(Collectors.toList());

	}

}
