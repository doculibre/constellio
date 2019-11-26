package com.constellio.model.services.schemas;

import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.dao.services.records.DataStore;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.KeyIntMap;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.ModificationImpact;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.constellio.model.services.schemas.SchemaUtils.areCacheIndex;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.ALL_REMOVED_AUTHS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.ATTACHED_ANCESTORS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.TOKENS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasInCollectionOf;
import static java.util.Arrays.asList;

//AFTER : Move in com.constellio.model.services.records.
public class ModificationImpactCalculator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModificationImpactCalculator.class);

	SearchServices searchServices;

	RecordServices recordServices;

	List<Taxonomy> taxonomies;

	MetadataSchemaTypes metadataSchemaTypes;

	SchemaUtils schemaUtils;

	public ModificationImpactCalculator(MetadataSchemaTypes metadataSchemaTypes, List<Taxonomy> taxonomies,
										SearchServices searchServices, RecordServices recordServices) {
		this(metadataSchemaTypes, taxonomies, searchServices, recordServices, new SchemaUtils());
	}

	public ModificationImpactCalculator(MetadataSchemaTypes metadataSchemaTypes, List<Taxonomy> taxonomies,
										SearchServices searchServices, RecordServices recordServices,
										SchemaUtils schemaUtils) {
		this.taxonomies = taxonomies;
		this.searchServices = searchServices;
		this.recordServices = recordServices;
		this.metadataSchemaTypes = metadataSchemaTypes;
		this.schemaUtils = schemaUtils;
	}

	public ModificationImpactCalculatorResponse findTransactionImpact(Transaction transaction,
																	  boolean executedAfterTransaction) {

		List<String> idsToReindex = new ArrayList<>();
		List<ModificationImpact> recordsModificationImpacts = new ArrayList<>();
		List<RecordsModification> recordsModifications = new RecordsModificationBuilder(recordServices)
				.build(transaction, metadataSchemaTypes);

		List<String> transactionRecordIds = executedAfterTransaction ? null : transaction.getRecordIds();

		for (RecordsModification recordsModification : recordsModifications) {
			for (ModificationImpact anImpact : findImpactOfARecordsModification(
					recordsModification, transactionRecordIds, transaction.getTitle())) {
				recordsModificationImpacts.add(anImpact);
			}

		}

		for (Record record : transaction.getRecords()) {

			if (Authorization.SCHEMA_TYPE.equals(record.getTypeCode())) {
				MetadataSchema authSchema = metadataSchemaTypes.getDefaultSchema(Authorization.SCHEMA_TYPE);
				Metadata authorizationPrincipals = authSchema.getMetadata(Authorization.PRINCIPALS);
				Metadata lastTokenRecalculate = authSchema.getMetadata(Authorization.LAST_TOKEN_RECALCULATE);
				Metadata authorizationTargetMetadata = authSchema.getMetadata(Authorization.TARGET);
				Metadata authorizationTargetSchemaTypeMetadata = authSchema.getMetadata(Authorization.TARGET_SCHEMA_TYPE);

				String authorizationTarget = record.get(authorizationTargetMetadata);
				String authorizationTargetSchemaType = record.get(authorizationTargetSchemaTypeMetadata);

				if (Authorization.isSecurableSchemaType(authorizationTargetSchemaType)
					&& (record.isModified(authorizationPrincipals) || record.isModified(lastTokenRecalculate))) {
					LogicalSearchCondition condition = fromAllSchemasInCollectionOf(record, DataStore.RECORDS)
							.where(Schemas.ATTACHED_ANCESTORS).isEqualTo(authorizationTarget);
					transaction.addAllRecordsToReindex(searchServices.searchRecordIds(condition));
				}

			}

		}

		return new ModificationImpactCalculatorResponse(recordsModificationImpacts, idsToReindex);
	}

	List<ModificationImpact> findImpactOfARecordsModification(RecordsModification recordsModification,
															  List<String> transactionRecordIds,
															  String transactionTitle) {
		List<ModificationImpact> impacts = new ArrayList<>();

		for (MetadataSchemaType type : metadataSchemaTypes.getSchemaTypes()) {
			impacts.addAll(findImpactsOfARecordsModificationInSchemaType(type, recordsModification, transactionRecordIds,
					transactionTitle));
		}
		return impacts;
	}

	List<ModificationImpact> findImpactsOfARecordsModificationInSchemaType(MetadataSchemaType schemaType,
																		   RecordsModification recordsModification,
																		   List<String> transactionRecordIds,
																		   String transactionTitle) {

		List<ModificationImpact> recordsModificationImpactsInType = new ArrayList<>();

		MetadataList references = new MetadataList();
		List<Metadata> reindexedMetadatas = new ArrayList<>();
		findPotentialMetadataToReindexAndTheirReferencesToAModifiedMetadata(schemaType, recordsModification,
				references, reindexedMetadatas);

		return findRealImpactsOfPotentialMetadataToReindex(schemaType, recordsModification, transactionRecordIds,
				recordsModificationImpactsInType, references, reindexedMetadatas, transactionTitle);
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

	private List<ModificationImpact> findRealImpactsOfPotentialMetadataToReindex(MetadataSchemaType schemaType,
																				 RecordsModification recordsModification,
																				 List<String> transactionRecordIds,
																				 List<ModificationImpact> recordsModificationImpactsInType,
																				 List<Metadata> references,
																				 List<Metadata> reindexedMetadatas,
																				 String transactionTitle) {
		if (!references.isEmpty()) {
			Iterator<List<Record>> batchIterator = splitModifiedRecordsInBatchOf1000(recordsModification);
			while (batchIterator.hasNext()) {

				List<Record> modifiedRecordsBatch = batchIterator.next();
				LogicalSearchCondition condition = getLogicalSearchConditionFor(schemaType, modifiedRecordsBatch,
						transactionRecordIds, references);
				int recordsCount;
				if (schemaType.getCacheType().hasPermanentCache() && areCacheIndex(references)) {
					Set<String> ids = new HashSet<>();
					for (Metadata referenceMetadata : references) {
						for (Record modifiedRecord : modifiedRecordsBatch) {
							for (String id : recordServices.getRecordsCaches()
									.getRecordsByIndexedMetadata(schemaType, referenceMetadata, modifiedRecord.getId())
									.map(Record::getId).collect(Collectors.toList())) {

								if (transactionRecordIds == null || !transactionRecordIds.contains(id)) {
									ids.add(id);
								}
							}
						}
					}
					recordsCount = ids.size();
					if (recordsCount > 0) {
						recordsModificationImpactsInType.add(new ModificationImpact(
								schemaType, reindexedMetadatas, condition, ids, recordsCount, transactionTitle));
					}

				} else {
					recordsCount = (int) searchServices.getResultsCount(condition);

					if (recordsCount > 0) {
						recordsModificationImpactsInType.add(new ModificationImpact(
								schemaType, reindexedMetadatas, condition, recordsCount, transactionTitle));
					}
				}


			}
		}

		return recordsModificationImpactsInType;
	}

	void findPotentialMetadataToReindexAndTheirReferencesToAModifiedMetadata(MetadataSchemaType schemaType,
																			 RecordsModification recordsModification,
																			 MetadataList references,
																			 List<Metadata> reindexedMetadatas) {
		List<Metadata> automaticMetadatas = schemaType.getAutomaticMetadatas();
		List<Metadata> modifiedMetadatas = recordsModification.getModifiedMetadatas();

		for (Metadata automaticMetadata : automaticMetadatas) {
			List<Metadata> referenceMetadatasLinkingToModifiedMetadatas = getReferenceMetadatasLinkingToModifiedMetadatas(
					automaticMetadata, modifiedMetadatas);
			if (!referenceMetadatasLinkingToModifiedMetadatas.isEmpty()) {
				reindexedMetadatas.add(automaticMetadata);
				references.addAll(referenceMetadatasLinkingToModifiedMetadatas);
			}
		}
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

}
