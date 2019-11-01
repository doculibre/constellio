package com.constellio.model.services.taxonomies;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServicesRuntimeException.TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess;
import com.constellio.model.services.taxonomies.queryHandlers.GetChildrenContext;
import com.constellio.model.services.taxonomies.queryHandlers.TaxonomiesSearchServicesLegacyQueryHandler;
import com.constellio.model.services.taxonomies.queryHandlers.TaxonomiesSearchServicesQueryHandler;
import com.constellio.model.services.taxonomies.queryHandlers.TaxonomiesSearchServicesRewrittenQueryHandler;

import java.util.Date;
import java.util.List;

public class TaxonomiesSearchServicesBasedOnHierarchyTokensImpl implements TaxonomiesSearchServices {

	//private static final String CHILDREN_QUERY = "children";
	private static final boolean NOT_LINKABLE = false;

	private ModelLayerFactory modelLayerFactory;

	public TaxonomiesSearchServicesBasedOnHierarchyTokensImpl(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	TaxonomiesSearchServicesQueryHandler handler(TaxonomiesSearchOptions options) {
		if (options.getFastContinueInfos() != null
			|| !Toggle.TRY_USING_NEW_CACHE_BASED_TAXONOMIES_SEARCH_SERVICES_QUERY_HANDLER.isEnabled()) {
			return new TaxonomiesSearchServicesLegacyQueryHandler(modelLayerFactory);
		} else {
			//return new TaxonomiesSearchServicesDefaultQueryHandler(modelLayerFactory);
			return new TaxonomiesSearchServicesRewrittenQueryHandler(modelLayerFactory);
		}
	}

	public List<TaxonomySearchRecord> getVisibleRootConcept(User user, String collection, String taxonomyCode,
															TaxonomiesSearchOptions options) {
		return getVisibleRootConceptResponse(user, collection, taxonomyCode, options, null).getRecords();
	}

	public List<TaxonomySearchRecord> getVisibleChildConcept(User user, String taxonomyCode, Record record,
															 TaxonomiesSearchOptions options) {
		return getVisibleChildConceptResponse(user, taxonomyCode, record, options).getRecords();
	}

	public boolean findNonTaxonomyRecordsInStructure(Record record, TaxonomiesSearchOptions options) {
		return handler(options).findNonTaxonomyRecordsInStructure(record, options);
	}

	public List<TaxonomySearchRecord> getLinkableRootConcept(User user, String collection, String taxonomyCode,
															 String selectedType, TaxonomiesSearchOptions options) {
		return getLinkableRootConceptResponse(user, collection, taxonomyCode, selectedType, options).getRecords();
	}

	public LinkableTaxonomySearchResponse getLinkableRootConceptResponse(User user, String collection,
																		 String usingTaxonomyCode,
																		 String selectedType,
																		 TaxonomiesSearchOptions options) {

		return getLinkableConceptResponse(user, collection, usingTaxonomyCode, selectedType, null, options);

	}

	public List<TaxonomySearchRecord> getLinkableChildConcept(User user, Record record, String usingTaxonomy,
															  String selectedType,
															  TaxonomiesSearchOptions options) {
		return getLinkableChildConceptResponse(user, record, usingTaxonomy, selectedType, options).getRecords();
	}

	public LinkableTaxonomySearchResponse getLinkableChildConceptResponse(User user, Record inRecord,
																		  String usingTaxonomy,
																		  String selectedType,
																		  TaxonomiesSearchOptions options) {

		return getLinkableConceptResponse(user, inRecord.getCollection(), usingTaxonomy, selectedType, inRecord, options);

	}

	public List<TaxonomySearchRecord> getVisibleChildConcept(User user, Record record,
															 TaxonomiesSearchOptions options) {

		GetChildrenContext ctx = new GetChildrenContext(user, record, options, null, modelLayerFactory);
		if (!ctx.hasPermanentCache()) {
			//Given new cache in v9.0, it is very weird for a taxonomy to not be cached
			return new TaxonomiesSearchServicesLegacyQueryHandler(modelLayerFactory)
					.getVisibleChildConcept(ctx);
		} else {
			return handler(options).getVisibleChildConcept(ctx);
		}

	}


	public LinkableTaxonomySearchResponse getVisibleChildConceptResponse(User user, String taxonomyCode, Record record,
																		 TaxonomiesSearchOptions options) {
		GetChildrenContext ctx = new GetChildrenContext(user, record, options, null, modelLayerFactory);
		if (!ctx.hasPermanentCache()) {
			//Given new cache in v9.0, it is very weird for a taxonomy to not be cached
			return new TaxonomiesSearchServicesLegacyQueryHandler(modelLayerFactory)
					.getVisibleChildrenRecords(ctx);
		} else {
			return handler(options).getVisibleChildrenRecords(ctx);
		}

	}

	public LinkableTaxonomySearchResponse getVisibleRootConceptResponse(User user, String collection,
																		String taxonomyCode,
																		TaxonomiesSearchOptions options,
																		String forSelectionOfSchemaType) {

		Taxonomy taxonomy = modelLayerFactory.getTaxonomiesManager().getEnabledTaxonomyWithCode(collection, taxonomyCode);

		MetadataSchemaType schemaType = forSelectionOfSchemaType == null ? null :
										modelLayerFactory.getMetadataSchemasManager()
												.getSchemaTypes(collection).getSchemaType(forSelectionOfSchemaType);

		GetChildrenContext ctx = new GetChildrenContext(user, null, options, schemaType, taxonomy, modelLayerFactory);
		if (!ctx.hasPermanentCache()) {
			//Given new cache in v9.0, it is very weird for a taxonomy to not be cached
			return new TaxonomiesSearchServicesLegacyQueryHandler(modelLayerFactory).getVisibleRootConceptResponse(ctx);

		} else {
			return handler(options).getVisibleRootConceptResponse(ctx);
		}
	}

	public boolean isLinkable(final Record record, final Taxonomy taxonomy, TaxonomiesSearchOptions options) {

		return handler(options).isLinkable(record, taxonomy, options);
	}

	private LinkableTaxonomySearchResponse getLinkableConceptResponse(User user, String collection,
																	  String usingTaxonomyCode,
																	  String selectedTypeCode, Record inRecord,
																	  TaxonomiesSearchOptions options) {

		long start = new Date().getTime();

		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		MetadataSchemaType selectedType = metadataSchemasManager.getSchemaTypes(collection).getSchemaType(selectedTypeCode);
		Taxonomy usingTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(collection, usingTaxonomyCode);
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(collection);

		LinkableTaxonomySearchResponse response;
		if (principalTaxonomy.getSchemaTypes().contains(selectedType.getCode())) {

			final Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(usingTaxonomy.getCollection(), usingTaxonomy.getCode());
			MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(taxonomy.getCollection())
					.getSchemaType(usingTaxonomy.getSchemaTypes().get(0));

			GetChildrenContext ctx = new GetChildrenContext(user, inRecord, options, schemaType, taxonomy, modelLayerFactory);

			if (!ctx.hasPermanentCache()) {
				//Given new cache in v9.0, it is very weird for a taxonomy to not be cached
				response = new TaxonomiesSearchServicesLegacyQueryHandler(modelLayerFactory)
						.getLinkableConceptsForSelectionOfAPrincipalTaxonomyConceptBasedOnAuthorizations(ctx);
			} else {
				response = handler(options).getLinkableConceptsForSelectionOfAPrincipalTaxonomyConceptBasedOnAuthorizations(ctx);
			}


		} else if (usingTaxonomy.getSchemaTypes().contains(selectedType.getCode())) {
			//selecting a record of a non-principal taxonomy
			if (Role.WRITE.equals(options.getRequiredAccess()) || Role.DELETE.equals(options.getRequiredAccess())) {
				throw new TaxonomiesSearchServicesRuntimeException_CannotFilterNonPrincipalConceptWithWriteOrDeleteAccess();
			}

			if (inRecord == null) {
				response = getVisibleRootConceptResponse(user, collection, usingTaxonomyCode, options,
						selectedTypeCode);
			} else {

				GetChildrenContext ctx = new GetChildrenContext(user, inRecord, options, selectedType, usingTaxonomy, modelLayerFactory);
				if (!ctx.hasPermanentCache()) {
					//Given new cache in v9.0, it is very weird for a taxonomy to not be cached
					response = new TaxonomiesSearchServicesLegacyQueryHandler(modelLayerFactory)
							.getVisibleChildrenRecords(ctx);
				} else {
					response = handler(options).getVisibleChildrenRecords(ctx);
				}
			}

		} else {
			//selecting a non-taxonomy record using a taxonomy
			GetChildrenContext ctx = new GetChildrenContext(user, inRecord, options, selectedType, usingTaxonomy, modelLayerFactory);

			if (!ctx.hasPermanentCache()) {
				//Given new cache in v9.0, it is very weird for a taxonomy to not be cached
				response = new TaxonomiesSearchServicesLegacyQueryHandler(modelLayerFactory)
						.getLinkableConceptsForSelectionOfARecordUsingNonPrincipalTaxonomy(ctx);
			} else {
				response = handler(options).getLinkableConceptsForSelectionOfARecordUsingNonPrincipalTaxonomy(ctx);
			}


		}

		long duration = new Date().getTime() - start;
		return response.withQTime(duration);
	}


	@Override
	public TaxonomiesSearchServicesCache getCache() {
		return modelLayerFactory.getTaxonomiesSearchServicesCache();
	}
}
