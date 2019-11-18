package com.constellio.model.services.taxonomies.queryHandlers;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.cacheIndexHook.impl.TaxonomyRecordsHookRetriever;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.HasChildrenQueryHandler;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.QueryExecutionMethod.IF_USING_INDEXED_METADATA;
import static com.constellio.model.services.search.query.logical.QueryExecutionMethod.USE_SOLR;

public class GetChildrenContext {
	User user;
	Record record;
	TaxonomiesSearchOptions options;
	MetadataSchemaType forSelectionOfSchemaType;
	MetadataSchemaType fromType;
	Taxonomy taxonomy;
	private boolean hasPermanentCache;
	ModelLayerFactory modelLayerFactory;
	boolean principalTaxonomy;
	List<MetadataSchemaType> classifiedSchemaTypes = Collections.emptyList();

	public GetChildrenContext(User user, Record record, TaxonomiesSearchOptions options,
							  MetadataSchemaType forSelectionOfSchemaType, ModelLayerFactory modelLayerFactory) {
		this.user = user;
		this.record = record;
		this.options = options;
		this.forSelectionOfSchemaType = forSelectionOfSchemaType;
		this.modelLayerFactory = modelLayerFactory;
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();

		this.taxonomy = taxonomiesManager.getTaxonomyOf(record);
		if (taxonomy == null) {
			taxonomy = taxonomiesManager.getPrincipalTaxonomy(record.getCollection());
		}

		if (taxonomy != null) {
			if (taxonomy.getSchemaTypes().size() == 1) {
				CacheConfig cacheConfig = modelLayerFactory.getRecordsCaches().getCache(getCollection()).getCacheConfigOf(taxonomy.getSchemaTypes().get(0));
				hasPermanentCache = cacheConfig != null && cacheConfig.isPermanent();
			}
			principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(getCollection()).hasSameCode(taxonomy);
			//			classifiedSchemaTypes = schemasManager.getSchemaTypes(taxonomy.getCollection())
			//					.getClassifiedSchemaTypesIn(taxonomy.getSchemaTypes().get(0));

		}

		fromType = schemasManager.getSchemaTypeOf(record);
		classifiedSchemaTypes = schemasManager.getSchemaTypes(taxonomy.getCollection())
				.getClassifiedSchemaTypesIn(fromType.getCode());

	}

	public TaxonomyRecordsHookRetriever getTaxonomyHookRetriever() {
		return getModelLayerFactory().getTaxonomyRecordsHookRetriever(getCollection());
	}

	public MetadataSchemaType getFromType() {
		return fromType;
	}

	public GetChildrenContext(User user, Record record, TaxonomiesSearchOptions options,
							  MetadataSchemaType forSelectionOfSchemaType, Taxonomy taxonomy,
							  ModelLayerFactory modelLayerFactory) {
		this.user = user;
		this.record = record;
		this.options = options;
		this.forSelectionOfSchemaType = forSelectionOfSchemaType;
		this.taxonomy = taxonomy;
		this.modelLayerFactory = modelLayerFactory;

		if (taxonomy != null) {
			if (taxonomy.getSchemaTypes().size() == 1) {
				CacheConfig cacheConfig = modelLayerFactory.getRecordsCaches().getCache(getCollection()).getCacheConfigOf(taxonomy.getSchemaTypes().get(0));
				hasPermanentCache = cacheConfig != null && cacheConfig.isPermanent();
			}
			principalTaxonomy = modelLayerFactory.getTaxonomiesManager().getPrincipalTaxonomy(getCollection()).hasSameCode(taxonomy);
		}

		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		if (record == null) {
			fromType = schemasManager.getSchemaTypes(taxonomy.getCollection()).getSchemaType(taxonomy.getSchemaTypes().get(0));
		} else {
			fromType = schemasManager.getSchemaTypeOf(record);
		}
		classifiedSchemaTypes = schemasManager.getSchemaTypes(fromType.getCollection())
				.getClassifiedSchemaTypesIn(fromType.getCode());

	}

	public boolean hasUserAccessToSomethingInConcept(Record record) {
		boolean write = Role.WRITE.equals(options.getRequiredAccess());
		boolean onlyVisible = options.getIncludeStatus() == StatusFilter.ACTIVES
							  && (this.forSelectionOfSchemaType == null || !this.getOptions().isShowInvisibleRecordsInLinkingMode());

		if (taxonomy != null && taxonomy.getSchemaTypes().contains(record.getTypeCode())) {
			if (isPrincipalTaxonomy()) {
				return getTaxonomyHookRetriever().hasUserAccessToSomethingInPrincipalConcept(user, record, write, onlyVisible);
			} else {
				return getTaxonomyHookRetriever().hasUserAccessToSomethingInSecondaryConcept(user, record.getRecordId(), write, onlyVisible);
			}
		} else {
			//Maybe... not worth the calculations
			return true;
		}

	}

	public boolean hasPermanentCache() {
		return hasPermanentCache;
	}

	public boolean hasRequiredAccessOn(Record record) {
		if (taxonomy != null && taxonomy.getSchemaTypes().contains(record.getTypeCode()) && !isPrincipalTaxonomy()) {
			return true;
		}
		return user.hasRequiredAccess(options.getRequiredAccess()).on(record);
	}

	public boolean isConceptOfNavigatedTaxonomy(Record childRecordLeadingToSecuredRecord) {
		return taxonomy.getSchemaTypes().contains(childRecordLeadingToSecuredRecord.getTypeCode());
	}

	public String getCollection() {
		return record == null ? taxonomy.getCollection() : record.getCollection();
	}

	/**
	 * Return list of queries used to build trees
	 * <p>
	 * Queries are ordered :
	 * <p>
	 * 1. The query for record of same type (ex. children administrative units, children folders)
	 * 2. The queries of parent other types (ex. folders if)
	 * 2. The queries of the given types (ex. documents in a folder)
	 *
	 * @return
	 */
	public List<LogicalSearchQuery> getQueries(boolean includeTaxonomyConcepts) {


		List<LogicalSearchQuery> queries = new ArrayList<>();

		if (forSelectionOfSchemaType != null) {
			List<String> recordTypesToShowInTree = new ArrayList<>();
			recordTypesToShowInTree.add(fromType.getCode());

			for (Metadata metadata : forSelectionOfSchemaType.getAllParentReferences()) {
				if (!recordTypesToShowInTree.contains(metadata.getReferencedSchemaType())) {
					recordTypesToShowInTree.add(metadata.getReferencedSchemaType());
				}
			}

			if (!recordTypesToShowInTree.contains(fromType.getCode())) {
				recordTypesToShowInTree.add(forSelectionOfSchemaType.getCode());
			}


			for (MetadataSchemaType schemaType : getModelLayerFactory().getMetadataSchemasManager()
					.getSchemaTypes(getCollection(), recordTypesToShowInTree)) {

				if (includeTaxonomyConcepts
					|| (taxonomy != null && taxonomy.getSchemaTypes().contains(schemaType.getCode()))) {

					for (Metadata referenceMetadata : schemaType.getAllMetadatas()) {

						if (referenceMetadata.getReferencedSchemaType().equals(fromType.getCode())
							&& (referenceMetadata.isTaxonomyRelationship() || referenceMetadata.isChildOfRelationship())) {

							if (record == null) {
								queries.add(new LogicalSearchQuery(from(schemaType).where(referenceMetadata).isNull())
										.setQueryExecutionMethod(IF_USING_INDEXED_METADATA));
							} else {
								queries.add(new LogicalSearchQuery(from(schemaType).where(referenceMetadata).isEqualTo(record.getId()))
										.setQueryExecutionMethod(IF_USING_INDEXED_METADATA));
							}
						}
					}

				}
			}

		} else {


			Consumer<MetadataSchemaType> addQueriesForType = new Consumer<MetadataSchemaType>() {
				@Override
				public void accept(MetadataSchemaType schemaType) {
					for (Metadata referenceMetadata : schemaType.getAllMetadatas()) {
						if (referenceMetadata.getReferencedSchemaType().equals(fromType.getCode())
							&& (referenceMetadata.isTaxonomyRelationship() || referenceMetadata.isChildOfRelationship())) {

							if (record == null) {
								queries.add(new LogicalSearchQuery(from(schemaType).where(referenceMetadata).isNull())
										.setQueryExecutionMethod(IF_USING_INDEXED_METADATA));
							} else {
								queries.add(new LogicalSearchQuery(from(schemaType).where(referenceMetadata).isEqualTo(record.getId()))
										.setQueryExecutionMethod(IF_USING_INDEXED_METADATA));
							}
						}
					}
				}
			};

			addQueriesForType.accept(fromType);

			for (MetadataSchemaType schemaType : getModelLayerFactory().getMetadataSchemasManager()
					.getSchemaTypes(getCollection()).getSchemaTypes()) {

				if (includeTaxonomyConcepts
					|| (taxonomy != null && taxonomy.getSchemaTypes().contains(schemaType.getCode()))) {

					if (!schemaType.getCode().equals(fromType.getCode()) && schemaType.getAllParentReferences().isEmpty()) {
						addQueriesForType.accept(schemaType);
					}

				}

			}

			for (MetadataSchemaType schemaType : getModelLayerFactory().getMetadataSchemasManager()
					.getSchemaTypes(getCollection()).getSchemaTypes()) {

				if (includeTaxonomyConcepts
					|| (taxonomy != null && taxonomy.getSchemaTypes().contains(schemaType.getCode()))) {

					if (!schemaType.getCode().equals(fromType.getCode()) && !schemaType.getAllParentReferences().isEmpty()) {
						addQueriesForType.accept(schemaType);
					}

				}
			}


		}

		return queries;
	}

	public LogicalSearchQuery newQueryWithUserFilter(LogicalSearchCondition condition) {
		LogicalSearchQuery logicalSearchQuery;
		if (user != null) {
			logicalSearchQuery = new LogicalSearchQuery(condition).filteredWithUserHierarchy(
					user, options.getRequiredAccess(), forSelectionOfSchemaType, options.isShowInvisibleRecordsInLinkingMode());

		} else {
			logicalSearchQuery = new LogicalSearchQuery(condition).setQueryExecutionMethod(USE_SOLR);
		}
		return logicalSearchQuery;
	}

	public boolean isHiddenInvisibleInTree() {
		return forSelectionOfSchemaType == null ? true : !options.isShowInvisibleRecordsInLinkingMode();
	}

	public boolean isSelectingAConcept() {
		return forSelectionOfSchemaType != null && taxonomy.getSchemaTypes().contains(forSelectionOfSchemaType.getCode());
	}


	public boolean isNonSecurableTaxonomyRecord(Record record) {
		return isConceptOfNavigatedTaxonomy(record) && !principalTaxonomy;
	}

	public String getCacheMode() {
		return HasChildrenQueryHandler.getCacheMode(forSelectionOfSchemaType, options.getRequiredAccess(),
				options.isShowInvisibleRecordsInLinkingMode(),
				options.isAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable());

	}

	public String username() {
		return user == null ? null : user.getUsername();

	}

	public GetChildrenContext createCopyFor(Record child) {
		return new GetChildrenContext(user, child, options, forSelectionOfSchemaType, taxonomy, modelLayerFactory);

	}

	public User getUser() {
		return user;
	}

	public Record getRecord() {
		return record;
	}

	public TaxonomiesSearchOptions getOptions() {
		return options;
	}

	public String getSelectedSchemaTypeCode() {
		return forSelectionOfSchemaType == null ? null : forSelectionOfSchemaType.getCode();
	}

	public boolean isShowingAllVisibleOrSelectingSchemaType(MetadataSchemaType schemaType) {
		return forSelectionOfSchemaType == null || forSelectionOfSchemaType.getCode().equals(schemaType.getCode());
	}

	public boolean isShowingAllVisibleOrSelectingSchemaType(String schemaTypeCode) {
		return forSelectionOfSchemaType == null || forSelectionOfSchemaType.getCode().equals(schemaTypeCode);
	}


	public boolean isShowingAllVisibleOrSelectingOtherSchemaType(MetadataSchemaType schemaType) {
		return forSelectionOfSchemaType == null || !forSelectionOfSchemaType.getCode().equals(schemaType.getCode());
	}

	public boolean isShowingAllVisibleOrSelectingOtherSchemaType(String schemaType) {
		return forSelectionOfSchemaType == null || !forSelectionOfSchemaType.getCode().equals(schemaType);
	}

	public MetadataSchemaType getForSelectionOfSchemaType() {
		return forSelectionOfSchemaType;
	}

	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

	public boolean isHasPermanentCache() {
		return hasPermanentCache;
	}

	public ModelLayerFactory getModelLayerFactory() {
		return modelLayerFactory;
	}

	public boolean isPrincipalTaxonomy() {
		return principalTaxonomy;
	}

	public List<MetadataSchemaType> getClassifiedSchemaTypes() {
		return classifiedSchemaTypes;
	}

	public Metadata getTaxonomyClassificationMetadata(MetadataSchemaType classifiedType) {


		for (Metadata metadata : classifiedType.getDefaultSchema().getMetadatas()) {
			if ((metadata.getType() == MetadataValueType.REFERENCE)
				&& fromType.getCode().equals(metadata.getReferencedSchemaType())
				&& metadata.getDataEntry().getType() == DataEntryType.MANUAL
			) {
				return metadata;
			}
		}

		return null;
	}
}
