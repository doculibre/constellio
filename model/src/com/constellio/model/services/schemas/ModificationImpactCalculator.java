package com.constellio.model.services.schemas;

import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.ALL_AUTHORIZATIONS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.ALL_REMOVED_AUTHS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.ATTACHED_ANCESTORS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.DETACHED_AUTHORIZATIONS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.INHERITED_AUTHORIZATIONS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.REMOVED_AUTHORIZATIONS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

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
			SearchServices searchServices, RecordServices recordServices, SchemaUtils schemaUtils) {
		this.taxonomies = taxonomies;
		this.searchServices = searchServices;
		this.recordServices = recordServices;
		this.metadataSchemaTypes = metadataSchemaTypes;
		this.schemaUtils = schemaUtils;
	}

	public List<ModificationImpact> findTransactionImpact(Transaction transaction,
			boolean executedAfterTransaction) {

		List<ModificationImpact> recordsModificationImpacts = new ArrayList<>();
		List<RecordsModification> recordsModifications = new RecordsModificationBuilder(recordServices)
				.build(transaction, metadataSchemaTypes);

		List<String> transactionRecordIds = executedAfterTransaction ? null : transaction.getRecordIds();

		for (RecordsModification recordsModification : recordsModifications) {
			recordsModificationImpacts.addAll(findImpactOfARecordsModification(recordsModification, transactionRecordIds));
		}
		return recordsModificationImpacts;
	}

	List<ModificationImpact> findImpactOfARecordsModification(RecordsModification recordsModification,
			List<String> transactionRecordIds) {
		List<ModificationImpact> impacts = new ArrayList<>();

		for (MetadataSchemaType type : metadataSchemaTypes.getSchemaTypes()) {
			impacts.addAll(findImpactsOfARecordsModificationInSchemaType(type, recordsModification, transactionRecordIds));
		}
		return impacts;
	}

	List<ModificationImpact> findImpactsOfARecordsModificationInSchemaType(MetadataSchemaType schemaType,
			RecordsModification recordsModification, List<String> transactionRecordIds) {

		List<ModificationImpact> recordsModificationImpactsInType = new ArrayList<>();

		MetadataList references = new MetadataList();
		List<Metadata> reindexedMetadatas = new ArrayList<>();
		findPotentialMetadataToReindexAndTheirReferencesToAModifiedMetadata(schemaType, recordsModification,
				references, reindexedMetadatas);

		return findRealImpactsOfPotentialMetadataToReindex(schemaType, recordsModification, transactionRecordIds,
				recordsModificationImpactsInType, references, reindexedMetadatas);
	}

	//	private List<ModificationImpact> findRealImpactsOfPotentialMetadataToReindex(MetadataSchemaType schemaType,
	//			RecordsModification recordsModification, List<String> transactionRecordIds,
	//			List<ModificationImpact> recordsModificationImpactsInType, List<Metadata> references,
	//			List<Metadata> reindexedMetadatas) {
	//		if (!references.isEmpty()) {
	//			Iterator<List<Record>> batchIterator = splitModifiedRecordsInBatchOf1000(recordsModification);
	//			while (batchIterator.hasNext()) {
	//
	//				LogicalSearchCondition facetsMainCondition = from(schemaType).whereAny(references).isNotNull();
	//				if (transactionRecordIds != null) {
	//					facetsMainCondition = facetsMainCondition.andWhere(Schemas.IDENTIFIER).isNotIn(transactionRecordIds);
	//				}
	//
	//				LogicalSearchQuery query = new LogicalSearchQuery(facetsMainCondition);
	//				List<Record> records = batchIterator.next();
	//				List<String> queries = new ArrayList<>();
	//				for (Record record : records) {
	//					LogicalSearchCondition facetCondition = from(schemaType).whereAny(references).isEqualTo(record.getId());
	//					queries.add(facetCondition.getSolrQuery());
	//				}
	//				query.addQueryFacets("batch", queries);
	//				query.setNumberOfRows(0);
	//
	//				SPEQueryResponse response = searchServices.query(query);
	//
	//				List<String> recordIdsWithModificationImpacts = new ArrayList<>();
	//				for(int i = 0 ; i < queries.size() ; i++) {
	//					Record record = records.get(i);
	//					String facetQuery = queries.get(i);
	//					if (response.getQueryFacetCount(facetQuery) > 0) {
	//						recordIdsWithModificationImpacts.add(record.getId());
	//					}
	//				}
	//
	//
	//				LogicalSearchCondition condition = getLogicalSearchConditionFor(schemaType, batchIterator.next(),
	//						transactionRecordIds, references);
	//				if (searchServices.hasResults(condition)) {
	//					recordsModificationImpactsInType.add(new ModificationImpact(reindexedMetadatas, condition));
	//				}
	//			}
	//		}
	//
	//		return recordsModificationImpactsInType;
	//	}

	LogicalSearchCondition getLogicalSearchConditionFor(MetadataSchemaType schemaType,
			List<Record> modifiedRecordsBatch, List<String> transactionRecordIds, List<Metadata> references) {
		LogicalSearchCondition condition = from(schemaType).whereAny(references).isIn(modifiedRecordsBatch);
		if (transactionRecordIds != null) {
			condition = condition.andWhere(Schemas.IDENTIFIER).isNotIn(transactionRecordIds);
		}

		return condition;
	}

	private List<ModificationImpact> findRealImpactsOfPotentialMetadataToReindex(MetadataSchemaType schemaType,
			RecordsModification recordsModification, List<String> transactionRecordIds,
			List<ModificationImpact> recordsModificationImpactsInType, List<Metadata> references,
			List<Metadata> reindexedMetadatas) {
		if (!references.isEmpty()) {
			Iterator<List<Record>> batchIterator = splitModifiedRecordsInBatchOf1000(recordsModification);
			while (batchIterator.hasNext()) {
				LogicalSearchCondition condition = getLogicalSearchConditionFor(schemaType, batchIterator.next(),
						transactionRecordIds, references);

				int recordsCount = (int) searchServices.getResultsCount(condition);
				if (recordsCount > 0) {
					recordsModificationImpactsInType.add(new ModificationImpact(
							schemaType, reindexedMetadatas, condition, recordsCount));
				}
			}
		}

		return recordsModificationImpactsInType;
	}

	void findPotentialMetadataToReindexAndTheirReferencesToAModifiedMetadata(MetadataSchemaType schemaType,
			RecordsModification recordsModification, MetadataList references, List<Metadata> reindexedMetadatas) {
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
				referencesToMetadata = Arrays.asList(getReferenceMetadataUsedByCopiedMetadata(automaticMetadata));
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

		} else if (dependency == SpecialDependencies.HIERARCHY
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
				|| (modifiedMeta.isLocalCode(ATTACHED_ANCESTORS) && automaticMeta.isLocalCode(ATTACHED_ANCESTORS))
				//				|| (modifiedMeta.isLocalCode(REMOVED_AUTHORIZATIONS) && automaticMeta.isLocalCode(ALL_REMOVED_AUTHS))
				//				|| (modifiedMeta.isLocalCode(DETACHED_AUTHORIZATIONS) && automaticMeta.isLocalCode(ALL_REMOVED_AUTHS))
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
